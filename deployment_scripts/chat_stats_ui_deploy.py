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

        app_dir = "chat-stats-ui"
        # path to the ChatStatsUI app

        tasks.execute(self.deploy, server_path, app_dir)

        env.hosts = []
        disconnect_all()

    def deploy(self, server_path, app_dir):
        with lcd("../ChatStatsUI"):
            current_dir = os.path.dirname(os.path.abspath(__file__))
            f = open(os.path.join(current_dir, "../ChatStatsUI/conf/application.conf"), "a")
            f.write("\nchat_home=\"" + server_path["app_path"] + "\"\n")
            f.close()
            local("gradle dist")

        # rm old target dir
        with cd(server_path["app_path"]):
            with cd(app_dir + "/conf/etc"):
                run("chmod 777 chat-stats-ui-init", warn_only=True)
                run("bash chat-stats-ui-init stop", warn_only=True, pty=False)
            run("rm -rf " + app_dir, warn_only=True)
        # put all required files into chat_stats_ui_path
        put("../ChatStatsUI/build/distributions/playBinary.zip", server_path["app_path"])
        with cd(server_path["app_path"]):
            # run app
            run("unzip playBinary")
            # remove the archive
            run("rm -f playBinary.zip")
            run("mv playBinary " + app_dir)

            with cd(app_dir):
                run("touch variables.conf")
                files.append("variables.conf", "homedir=\"" + server_path["app_path"] + "/" + app_dir + "\"")
                files.append("variables.conf", "exec=\"${homedir}/bin/playBinary\"")
                files.append("variables.conf", "JAVA_HOME=\"" + server_java_home + "\"")

                with cd("conf"):
                    # in case "resources" dir is absent
                    run("mkdir resources", warn_only=True)

                run("chmod 777 conf/etc/chat-stats-ui-init")
                run("bash conf/etc/chat-stats-ui-init start", pty=False)


if __name__ == "__main__":

    chat_host = os.environ["chat_host"]
#    chat_host = "stage3"
    print "chat_host: " + chat_host

    server_java_home = os.environ["server_java_home"]
#    server_java_home = "/usr/lib/jvm/jre-1.8.0-openjdk.x86_64"
    print "server_java_home: " + server_java_home

    app = ChatStatsUIDeployApp()
    app.run_deployment()


