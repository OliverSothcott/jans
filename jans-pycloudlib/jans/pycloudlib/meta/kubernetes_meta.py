"""This module consists of class to interact with Kubernetes API."""

from __future__ import annotations

import logging
import os
import shlex
import tarfile
import typing as _t
from tempfile import TemporaryFile

import kubernetes.client
import kubernetes.config
from kubernetes.stream import stream

from jans.pycloudlib.meta.base_meta import BaseMeta

if _t.TYPE_CHECKING:  # pragma: no cover
    # imported objects for function type hint, completion, etc.
    # these won't be executed in runtime
    from kubernetes.client.models import V1Pod


logger = logging.getLogger(__name__)


class KubernetesMeta(BaseMeta):
    """A class to interact with a subset of Kubernetes APIs."""

    def __init__(self) -> None:
        self._client: _t.Union[kubernetes.client.CoreV1Api, None] = None
        self.kubeconfig_file = os.path.expanduser("~/.kube/config")

    @property
    def client(self) -> kubernetes.client.CoreV1Api:
        """Get kubernetes client instance."""
        if not self._client:
            # config loading priority
            try:
                kubernetes.config.load_incluster_config()
            except kubernetes.config.config_exception.ConfigException:
                kubernetes.config.load_kube_config(self.kubeconfig_file)
            self._client = kubernetes.client.CoreV1Api()
            self._client.api_client.configuration.assert_hostname = False
        return self._client

    def get_containers(self, label: str) -> list[V1Pod]:
        """Get list of pods based on label in a namespace.

        The namespace is resolved from value of ``CN_CONTAINER_METADATA_NAMESPACE``
        environment variable.

        :params label: Label name, i.e. ``APP_NAME=oxauth``.
        :returns: List of pod objects.
        """
        namespace = os.environ.get("CN_CONTAINER_METADATA_NAMESPACE", "default")
        pods: list[V1Pod] = self.client.list_namespaced_pod(namespace, label_selector=label).items
        return pods

    def get_container_ip(self, container: V1Pod) -> str:
        """Get container's IP address.

        :params container: Pod object.
        :returns: IP address associated with the pod.
        """
        ip: str = container.status.pod_ip
        return ip

    def get_container_name(self, container: V1Pod) -> str:
        """Get container's name.

        :params container: Pod object.
        :returns: Pod name.
        """
        name: str = container.metadata.name
        return name

    def copy_to_container(self, container: V1Pod, path: str) -> None:
        """Copy path to container.

        :params container: Pod object.
        :params path: Path to file or directory.
        """
        # make sure parent directory is created first
        dirname = os.path.dirname(path)
        self.exec_cmd(container, f"mkdir -p {dirname}")

        # copy file implementation
        resp = stream(
            self.client.connect_get_namespaced_pod_exec,
            container.metadata.name,
            container.metadata.namespace,
            command=["tar", "xmvf", "-", "-C", "/"],
            container=self._get_main_container_name(container),
            stderr=True,
            stdin=True,
            stdout=True,
            tty=False,
            _preload_content=False,
        )

        with TemporaryFile() as tar_buffer:
            with tarfile.open(fileobj=tar_buffer, mode="w") as tar:
                tar.add(path)

            tar_buffer.seek(0)
            commands = [tar_buffer.read()]

            while resp.is_open():
                resp.update(timeout=1)

                if resp.peek_stdout():
                    logger.debug(f"STDOUT: {resp.read_stdout()}")

                if resp.peek_stderr():
                    logger.debug(f"STDERR: {resp.read_stderr()}")

                if commands:
                    c = commands.pop(0)
                    resp.write_stdin(c)
                else:
                    break
            resp.close()

    def exec_cmd(self, container: V1Pod, cmd: str) -> _t.Any:
        """Run command inside container.

        :params container: Pod object.
        :params cmd: String of command.
        """
        return stream(
            self.client.connect_get_namespaced_pod_exec,
            container.metadata.name,
            container.metadata.namespace,
            command=shlex.split(cmd),
            container=self._get_main_container_name(container),
            stderr=True,
            stdin=True,
            stdout=True,
            tty=False,
        )

    def _get_main_container_name(self, container: V1Pod) -> str:
        """Get the pod's main container name.

        The main container name is determined from the value of ``CN_CONTAINER_MAIN_NAME``
        environment variable set in the pod.
        If the value is empty, fallback to the first container inside the pod.

        :param container: Pod object.
        """
        name = ""
        for cntr in container.spec.containers:
            if not cntr.env:
                continue

            for env in cntr.env:
                if env.name == "CN_CONTAINER_MAIN_NAME":
                    name = env.value
                    break

        # add fallback (if needed)
        return name or container.spec.containers[0].name
