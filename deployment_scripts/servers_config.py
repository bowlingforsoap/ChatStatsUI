__author__ = 'igorkhomenko'

servers = {
    "prod": {
        "hosts": ["qbchat01.quickblox.com"],
        "user": "chat_prod_run",
        "app_path": "/home/applications/chat_prod/QuickBlox-Chat"
    },
    "stage1": {
        "hosts": ["54.152.19.172"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "stage2": {
        "hosts": ["52.5.186.208"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "stage3": {
        "hosts": ["adminstage3.quickblox.com"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "stage4": {
        "hosts": ["52.2.179.79", "54.210.93.127", "52.2.163.37"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
    },
    "stage4_muc": {
        "hosts": ["52.2.163.37"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
    },
    "playdemic": {
        "hosts": ["52.0.13.172"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "tuul_dev": {
        "hosts": ["54.187.165.203"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
        "group": "tuul"
    },
    "tuul_qa": {
        "hosts": ["ec2-54-69-96-27.us-west-2.compute.amazonaws.com"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
        "group": "tuul"
    },
    "axel_prod": {
        "hosts": ["54.93.38.225", "54.93.41.176"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
        "group": "axel"
    },
    "axel_prod_muc": {
        "hosts": ["54.93.83.36", "54.93.90.237"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC",
        "group": "axel"
    },
    "axel_loadtests": {
        "hosts": ["52.28.122.61", "52.28.122.62"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
        "group": "axel"
    },
    "axel_loadtests_muc": {
        "hosts": ["52.28.79.37", "52.28.84.26"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC",
        "group": "axel"
    },
    "axel_dev": {
        "hosts": ["54.93.67.140"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
        "group": "axel"
    },
    "mlbpa": {
        "hosts": ["54.88.250.15"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "wamedia": {
        "hosts": ["54.165.111.12"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "kagiso": {
        "hosts": ["54.169.207.233"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "playdemic_old": {
        "hosts": ["52.0.13.172"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "kijiji_qa": {
        "hosts": ["54.149.200.3", "54.213.193.221"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "kijiji_qa_muc": {
        "hosts": ["54.148.96.128", "54.149.152.230"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC"
    },
    "kijiji_prod": {
        "hosts": ["52.10.103.183", "52.11.127.43"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "kijiji_prod_muc": {
        "hosts": ["52.11.137.106", "52.11.145.48"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC"
    },
    "kms_chat": {
        "hosts": ["52.74.29.17"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "schumacher-dev": {
        "hosts": ["54.175.105.48"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "realstir_chat": {
        "hosts": ["54.191.163.130"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "realstir_muc": {
        "hosts": ["52.11.215.154"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC"
    },
    "bancsabadell": {
        "hosts": ["ec2-79-125-71-138.eu-west-1.compute.amazonaws.com"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "tt8": {
        "hosts": ["52.6.123.165"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "pager": {
        "hosts": ["52.74.72.39"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "wavechat": {
        "hosts": ["54.148.121.167", "54.200.10.9"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "schumacher-prod": {
        "hosts": ["52.6.34.82"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "entrada": {
        "hosts": ["52.5.148.246"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "shape": {
        "hosts": ["23.23.156.130"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "pingin": {
        "hosts": ["54.72.186.223"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "wamedia-chat": {
        "hosts": ["54.165.111.12"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "bsdenterprise": {
        "hosts": ["52.6.170.42"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "ifreex": {
        "hosts": ["54.207.75.116"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "tt12": {
        "hosts": ["52.7.196.251"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "trial": {
        "hosts": ["52.6.193.95"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },    
    "satkirit-chat": {
        "hosts": ["54.171.222.222"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "satkirit-chat": {
        "hosts": ["54.171.222.222"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "indutechlabs-chat": {
        "hosts": ["52.74.213.177"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "ext-muc-stage": {
        "hosts": ["52.5.96.148"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "dateifi": {
        "hosts": ["52.28.127.100"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "as-test-chat-1": {
        "hosts": ["52.28.122.61"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "as-test-chat-2": {
        "hosts": ["52.28.122.62"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "as-test-muc-1": {
        "hosts": ["52.28.79.37"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC"
    },
    "as-test-muc-2": {
        "hosts": ["52.28.127.239"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC"
    },
    "fasbain": {
        "hosts": ["52.74.65.202"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "marcoapp": {
        "hosts": ["52.7.22.206"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "wolf": {
        "hosts": ["52.0.197.56"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "vronetwork": {
        "hosts": ["52.74.3.247"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "touchsurgery": {
        "hosts": ["54.76.228.101"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "test10": {
        "hosts": ["54.86.196.233"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "hopcam": {
        "hosts": ["54.173.2.35", "52.7.171.133"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "liveclinic": {
        "hosts": ["52.8.182.108"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "cht": {
        "hosts": ["52.17.250.115"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "seniorsome": {
        "hosts": ["54.77.135.118"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "avatarphone": {
        "hosts": ["52.69.44.128"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "seemee": {
        "hosts": ["54.79.70.216"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "mlbpa": {
        "hosts": ["54.88.250.15"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "epsyclinic": {
        "hosts": ["54.169.44.162"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "fieldbit": {
        "hosts": ["54.208.247.80"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "incommon": {
        "hosts": ["52.18.110.54"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "hiorbit": {
        "hosts": ["52.27.167.169"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "caresharing": {
        "hosts": ["52.18.10.127"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "testssl": {
        "hosts": ["52.27.214.70"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "founderfoxdev": {
        "hosts": ["52.25.54.190"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "founderfoxprod": {
        "hosts": ["52.26.154.187"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "beaboss": {
        "hosts": ["52.3.58.111"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "taxinet": {
        "hosts": ["52.2.6.139"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "stage5": {
        "hosts": ["52.3.166.134"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "sg": {
        "hosts": ["52.28.214.247"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "hitchat": {
        "hosts": ["54.207.110.215"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "telecom": {
        "hosts": ["54.153.111.42"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat"
    },
    "axel_dev_chat2": {
        "hosts": ["52.28.230.220"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat",
        "group": "axel"
    },
    "axel_dev_muc": {
        "hosts": ["52.28.230.232"],
        "user": "qb_chat",
        "app_path": "/home/qb_chat/QuickBlox-Chat-MUC",
        "group": "axel"
    },
}
