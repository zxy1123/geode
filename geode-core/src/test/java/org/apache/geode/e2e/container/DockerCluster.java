package org.apache.geode.e2e.container;

import static com.google.common.base.Charsets.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;

public class DockerCluster {

  private DockerClient docker;
  private int locatorCount;
  private int serverCount;
  private String name;
  private List<String> nodeIds;
  private String locatorAddress;

  public DockerCluster(String name, int serverCount) {
    this(name, 1, serverCount);
  }

  public DockerCluster(String name, int locatorCount, int serverCount) {
    docker = DefaultDockerClient.builder().
      uri("unix:///var/run/docker.sock").build();

    this.name = name;
    this.locatorCount = locatorCount;
    this.serverCount = serverCount;
    this.nodeIds = new ArrayList<>();
  }

  public void start() throws Exception {
    startLocators();
    startServers();
  }

  public String startContainer(int index) throws DockerException, InterruptedException {
    String geodeHome = System.getenv("GEODE_HOME");
    String vol = String.format("%s:/tmp/work", geodeHome);

    HostConfig hostConfig = HostConfig.
      builder().
      appendBinds(vol).
      build();

    ContainerConfig config = ContainerConfig.builder().
      image("gemfire/ubuntu-gradle").
      openStdin(true).
      hostname(String.format("%s-%d", name, index)).
      hostConfig(hostConfig).
      workingDir("/tmp").
      build();

    ContainerCreation creation = docker.createContainer(config);
    String id = creation.id();
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
        String.format("--name=%s-locator-%d", name, i)
      };

      String id = startContainer(i);
      execCommand(id, true, null, command);

      while (gfshCommand(null, null) != 0) {
        Thread.sleep(250);
      }
    }

    locatorAddress = String.format("%s[10334]", docker.inspectContainer(nodeIds.get(0)).networkSettings().ipAddress());
  }

  public void startServers() throws DockerException, InterruptedException {
    for (int i = 0; i < serverCount+1; i++) {
      String[] command = {
        "/tmp/work/bin/gfsh",
        "start server",
        String.format("--name=%s-server-%d", name, i),
        String.format("--locators=%s", locatorAddress)
      };

      String id = startContainer(i);
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
    execParams.add(DockerClient.ExecCreateParam.attachStdout());
    execParams.add(DockerClient.ExecCreateParam.attachStderr());
    execParams.add(DockerClient.ExecCreateParam.detach(startDetached));

    String execId = docker.execCreate(id, command, execParams.toArray(new DockerClient.ExecCreateParam[]{}));
    LogStream output = docker.execStart(execId);

    if (startDetached) {
      return 0;
    }

    StringBuilder buffer = new StringBuilder();
    while (output.hasNext()) {
      String multiLine = UTF_8.decode(output.next().content()).toString();
      buffer.append(multiLine);

      if (buffer.indexOf("\n") >= 0) {
        int n;
        while ((n = buffer.indexOf("\n")) >=0 ) {
          System.out.println("[gfsh]: " + buffer.substring(0, n));
          if (callback != null) {
            callback.call(buffer.substring(0, n));
          }
          buffer = new StringBuilder(buffer.substring(n + 1));
        }
      }
    }

    return docker.execInspect(execId).exitCode();
  }

  public void stop() throws DockerException, InterruptedException {
    for (String id : nodeIds) {
      docker.killContainer(id);
      docker.removeContainer(id);
    }
    docker.close();
  }

  public String getLocatorAddress() {
    return locatorAddress;
  }
}
