__author__ = 'Strelchenko Vadym'
#!/usr/bin/python

from fabric.operations import put, local
from fabric import tasks
from fabric.api import run, env
from fabric.network import disconnect_all
from fabric.context_managers import cd, lcd
from fabric.colors import green
from fabric.contrib import files

import os
import servers_config

class ChatStatsUIDeployApp:

    def run_deployment(self):
        try:
            server_path = servers_config.servers[chat_host]
        except KeyError:
            raise Exception('No server in config')

        # set hosts
        for path in server_path["hosts"]:
            path_with_user = server_path["user"] + "@" + path
            env.hosts.append(path_with_user)

        print(green("\nHosts: " + str(env.hosts) + "\n"))

        app_name_version = str.lower(app_name) + "-" + version
        # path to the ChatStatsUI app

        tasks.execute(self.deploy, server_path, app_name_version)

        env.hosts = []
        disconnect_all()

    def deploy(self, server_path, app_name_version):
        with lcd("../ChatStatsUI"):
            # sanity check
            local("rm -rf target/universal")
            # distribute ChatStatsUI in prod mode
            local("chmod 777 activator")
            local("./activator dist -java-home /usr/lib/jvm/java-1.8.0-openjdk.x86_64")

        # rm old target dir
        with cd(server_path["app_path"]):
            with cd(app_name_version + "/conf/etc"):
                run("chmod 777 " + app_name_version + "chat-stats-ui-init", warn_only=True)
                run("bash chat-stats-ui-init stop", warn_only=True, pty=False)
            run("rm -rf " + app_name_version, warn_only=True)
        # put all required files into chat_stats_ui_path
        put("../ChatStatsUI/target/universal/" + app_name_version + ".zip", server_path["app_path"])
        with cd(server_path["app_path"]):
            # run app
            run("unzip -o " + app_name_version)
            # remove the archive
            run("rm -f " + app_name_version + ".zip")

            with cd(app_name_version):
                run("touch variables.conf")
                files.append("variables.conf", "homedir=\"" + server_path["app_path"] + "/" + app_name_version + "\"")
                files.append("variables.conf", "exec=\"${homedir}/bin/" + str.lower(app_name) + "\"")
                files.append("variables.conf", "prog=\"" + app_name + "\"")
                files.append("variables.conf", "OPTS=\"-java-home " + server_java_home +  "\"")

                with cd("conf"):
                    # set chat_home in conf/application.conf
                    files.append("application.conf", "chat_home=\"" + server_path["app_path"] + "\"")
                    # in case "resources" dir is absent
                    run("mkdir resources")

                # with cd("bin"):
                #     print("Starting application in new screen: 'play'")
                #     run("screen -S play -d -m ./" + str.lower(app_name) + " -java-home " + server_java_home, pty=False)
                #     print("Use 'screen -ls' to check all the screens on the server")
                    with cd("etc"):
                        run("chmod 777 chat-stats-ui-init")
                        run("bash chat-stats-ui-init start", pty=False)


if __name__ == "__main__":

    chat_host = os.environ["chat_host"]
    # chat_host = "stage1"
    print "chat_host: " + chat_host

    app_name = os.environ["app_name"]
    # app_name = "ChatStatsUI"
    print "app_name: " + app_name

    version = os.environ["version"]
    # version = "1.0-SNAPSHOT"
    print "version: " + version

    server_java_home = os.environ["server_java_home"]
    # server_java_home = "/usr/lib/jvm/jre-1.8.0-openjdk.x86_64"
    print "server_java_home: " + server_java_home

    app = ChatStatsUIDeployApp()
    app.run_deployment()
