package org.apache.geode.container;

import static com.google.common.base.Charsets.*;

import java.util.ArrayList;
import java.util.Arrays;
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
      execCommand(id, command);
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
      execCommand(id, command);
    }
  }

  public int gfshCommand(String command) throws DockerException, InterruptedException {
    String locatorId = nodeIds.get(0);
    List<String> gfshCmd = Arrays.asList(command);
    gfshCmd.add(0, "/tmp/work/bin/gfsh");
    return execCommand(locatorId, gfshCmd.toArray(new String[]{}));
  }

  public int execCommand(String id, String... command) throws DockerException, InterruptedException {
    String execId = docker.execCreate(id, command, DockerClient.ExecCreateParam.attachStdout(), DockerClient.ExecCreateParam
      .attachStderr());
    LogStream output = docker.execStart(execId);

    while (output.hasNext()) {
      System.out.print(UTF_8.decode(output.next().content()));
      System.out.flush();
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
