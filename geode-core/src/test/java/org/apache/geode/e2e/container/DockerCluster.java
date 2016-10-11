package org.apache.geode.e2e.container;

import static com.google.common.base.Charsets.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;

public class DockerCluster {

  private DockerClient docker;
  private int locatorCount;
  private int serverCount;
  private String name;
  private List<String> nodeIds;
  private String locatorHost;
  private int locatorPort;
  private final String geodeHome;
  private final String scratchDir;

  private static final String SCRATCH_DIR_BASENAME = "scratch";

  public DockerCluster(String name) throws IOException {
    geodeHome = System.getenv("GEODE_HOME");
    if (geodeHome == null) {
      throw new IllegalStateException("GEODE_HOME environment variable is not set");
    }

    Path scratch = Files.createDirectory(Paths.get(geodeHome, SCRATCH_DIR_BASENAME));
    scratch.toFile().deleteOnExit();
    scratchDir = scratch.toString();

    docker = DefaultDockerClient.builder().
      uri("unix:///var/run/docker.sock").build();

    this.name = name;
    this.nodeIds = new ArrayList<>();
  }

  public void setServerCount(int count) {
    this.serverCount = count;
  }

  public void setLocatorCount(int count) {
    this.locatorCount = count;
  }

  public String getScratchDir() {
    return scratchDir;
  }

  /**
   * Given a file on the local host's filesystem, copy that file into the cluster's
   * global scratch area. The scratch file is deleted on exit.
   * @param hostFile
   * @return the cluster relative file name - for example /tmp/work/scratch/foo.zip
   */
  public String injectScratchFile(String hostFile) throws IOException {
    Path localPath = Paths.get(hostFile);
    Path scratchFile = Paths.get(getScratchDir(), localPath.getFileName().toString());
    Files.copy(localPath, scratchFile);
    scratchFile.toFile().deleteOnExit();

    return Paths.get("/", "tmp", "work", SCRATCH_DIR_BASENAME, scratchFile.getFileName().toString()).toString();
  }

  public void start() throws Exception {
    startLocators();
    startServers();
  }

  public String startContainer(int index) throws DockerException, InterruptedException {
    return startContainer(index, new HashMap<>());
  }

  public String startContainer(int index, Map<String, List<PortBinding>> portBindings) throws DockerException, InterruptedException {
    String vol = String.format("%s:/tmp/work", geodeHome);
    String hostname = String.format("%s-%d", name, index);

    HostConfig hostConfig = HostConfig.
      builder().
      portBindings(portBindings).
      appendBinds(vol).
      build();

    ContainerConfig config = ContainerConfig.builder().
      image("gemfire/ubuntu-gradle").
      openStdin(true).
      exposedPorts(portBindings.keySet()).
      hostname(hostname).
      hostConfig(hostConfig).
      workingDir("/tmp").
      build();

    ContainerCreation creation = docker.createContainer(config);
    String id = creation.id();
//    docker.renameContainer(id, hostname);
    docker.startContainer(id);
    docker.inspectContainer(id);

    nodeIds.add(id);

    return id;
  }

  public void startLocators() throws DockerException, InterruptedException {
    for (int i = 0; i < locatorCount; i++) {
      String[] command = {
        "/tmp/work/bin/gfsh",
        "start locator",
        String.format("--name=%s-locator-%d", name, i),
        "--J=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
      };

      Map<String, List<PortBinding>> ports = new HashMap<>();
      List<PortBinding> binding = new ArrayList<>();
      binding.add(PortBinding.of("HostPort", (10334 + i) + ""));
      ports.put((10334 + i) + "/tcp", binding);

      List<PortBinding> debugBinding = new ArrayList<>();
      debugBinding.add(PortBinding.of("HostPort", (5005 + i) + ""));
      ports.put("5005/tcp", debugBinding);

      String id = startContainer(i, ports);
      execCommand(id, true, null, command);

      while (gfshCommand(null, null) != 0) {
        Thread.sleep(250);
      }
    }

    // Let's get the host port which is mapped to the container 10334 port
    NetworkSettings networks = docker.inspectContainer(nodeIds.get(0)).networkSettings();
    locatorPort = Integer.parseInt(networks.ports().get("10334/tcp").get(0).hostPort());
  }

  public void startServers() throws DockerException, InterruptedException {
    String locatorAddress = String.format("%s[10334]", docker.inspectContainer(nodeIds.get(0)).networkSettings().ipAddress());
    for (int i = 0; i < serverCount; i++) {
      String serverPort = Integer.toString(40404 + i);
      String[] command = {
        "/tmp/work/bin/gfsh",
        "start server",
        String.format("--name=%s-server-%d", name, i),
        "--locators=" + locatorAddress,
        "--server-port=" + serverPort,
        "--hostname-for-clients=localhost",
        "--J=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
      };

      Map<String, List<PortBinding>> ports = new HashMap<>();
      List<PortBinding> binding = new ArrayList<>();
      binding.add(PortBinding.of("HostPort", serverPort));
      ports.put(serverPort + "/tcp", binding);

      List<PortBinding> debugBinding = new ArrayList<>();
      debugBinding.add(PortBinding.of("HostPort", (5005 + i + locatorCount) + ""));
      ports.put("5005/tcp", debugBinding);

      String id = startContainer(i, ports);
      execCommand(id, true, null, command);
    }

    int runningServers = 0;
    while (runningServers != serverCount) {
      Thread.sleep(200);

      List<String> cmdOutput = new ArrayList<>();

      ResultCallback cb = line -> cmdOutput.add(line);
      gfshCommand("list members", cb);

      runningServers = 0;
      for (int i = 0; i < serverCount; i++) {
        String server = String.format("%s-server-%d", name, i);
        for (String s : cmdOutput) {
          if (s.startsWith(server)) {
            runningServers++;
          }
        }
      }
    }
  }

  public int gfshCommand(String command) throws DockerException, InterruptedException {
    return gfshCommand(command, null);
  }

  public int gfshCommand(String command, ResultCallback callback) throws DockerException, InterruptedException {
    String locatorId = nodeIds.get(0);
    List<String> gfshCmd = new ArrayList<>();
    Collections.addAll(gfshCmd, "/tmp/work/bin/gfsh", "-e", "connect --jmx-locator=localhost[1099]");

    if (command != null) {
      Collections.addAll(gfshCmd, "-e", command);
    }

    return execCommand(locatorId, false, callback, gfshCmd.toArray(new String[]{}));
  }

  public int execCommand(String id, boolean startDetached,
                         ResultCallback callback, String... command) throws DockerException, InterruptedException {
    List<DockerClient.ExecCreateParam> execParams = new ArrayList<>();

    if (startDetached) {
      execParams.add(DockerClient.ExecCreateParam.detach(startDetached));
    } else {
      execParams.add(DockerClient.ExecCreateParam.attachStdout());
      execParams.add(DockerClient.ExecCreateParam.attachStderr());
    }

    String execId = docker.execCreate(id, command, execParams.toArray(new DockerClient.ExecCreateParam[]{}));

    LogStream output = docker.execStart(execId);

    if (startDetached) {
      output.close();
      return 0;
    }

    StringBuilder buffer = new StringBuilder();
    while (output.hasNext()) {
      String multiLine = UTF_8.decode(output.next().content()).toString();
      buffer.append(multiLine);

      if (buffer.indexOf("\n") >= 0) {
        int n;
        while ((n = buffer.indexOf("\n")) >= 0) {
          System.out.println("[gfsh]: " + buffer.substring(0, n));
          if (callback != null) {
            callback.call(buffer.substring(0, n));
          }
          buffer = new StringBuilder(buffer.substring(n + 1));
        }
      }
    }

    output.close();

    return docker.execInspect(execId).exitCode();
  }

  public void stop() throws DockerException, InterruptedException {
    for (String id : nodeIds) {
      try {
        docker.killContainer(id);
        docker.removeContainer(id);
      } catch (ContainerNotFoundException nex) {
        // Ignored because a container may have already been killed by the test
      }
    }
    docker.close();
  }

  public String getLocatorHost() {
    return locatorHost;
  }

  public int getLocatorPort() {
    return locatorPort;
  }

  public void killServer(int idx) throws DockerException, InterruptedException {
    String id = nodeIds.get(idx + locatorCount);
    if (id == null) {
      throw new IllegalArgumentException("Could not find server with index: " + idx);
    }
    docker.killContainer(id);
    docker.removeContainer(id);
  }

  public static void scorch() throws DockerException, InterruptedException {
    DockerClient docker = DefaultDockerClient.builder().
      uri("unix:///var/run/docker.sock").build();

    List<Container> containers = docker.listContainers();
    for (Container c : containers) {
      docker.stopContainer(c.id(), 0);
      docker.removeContainer(c.id());
    }
  }
}
