from flask import Flask, request, jsonify
from config import BaseConfig
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
from flask_cors import CORS
import requests
import threading

app = Flask(__name__)

CORS(app)  # 全局启用CORS，允许所有来源

DEREGISTER_URL = '/deregister'  # agent侧提供的注销接口
COLLECT_CONTROL_URL = '/cc'  # agent侧提供的采集启停接口
POLICY_RECEIVE_URL = '/receive'  # agent侧提供的采集策略接收接口
AGENT_PORT = 7777  # agent侧端口

class JsonResponse:
    @staticmethod
    def success(data=None, code=200, msg='success'):
        return jsonify({
            'code': code,
            'msg': msg,
            'data': data
        })

    @staticmethod
    def error(msg='error', code=400):
        return jsonify({
            'code': code,
            'msg': msg,
            'data': None
        })

app.config.from_object(BaseConfig)
db = SQLAlchemy(app)

print('>>> mysql url: ', app.config['SQLALCHEMY_DATABASE_URI'])

class Agent(db.Model):
    __tablename__ = 't_agent'  # 真实表名
    id = db.Column(db.Integer, primary_key=True)
    agent_identifier = db.Column(db.String(128), unique=False, nullable=True)
    agent_ip = db.Column(db.String(32), unique=False, nullable=True)
    agent_group = db.Column(db.String(64), unique=False, nullable=True)
    agent_status = db.Column(db.String(16), unique=False, nullable=True)  # online or offline
    create_time = db.Column(db.String(32), unique=False, nullable=True)

    def to_dict(self):
        return {
            'id': self.id,
            'agent_identifier': self.agent_identifier,
            'agent_ip': self.agent_ip,
            'agent_group': self.agent_group,
            'agent_status': self.agent_status,
            'create_time': self.create_time.strftime('%Y-%m-%d %H:%M:%S')
        }

class LogMsg(db.Model):
    __tablename__ = 't_log_msg'  # 真实表名
    id = db.Column(db.Integer, primary_key=True)
    log_msg = db.Column(db.Text, nullable=True)
    create_time = db.Column(db.String(32), unique=False, nullable=True)

    def to_dict(self):
        return {
            'id': self.id,
            'log_msg': self.log_msg,
            'create_time': self.create_time.strftime('%Y-%m-%d %H:%M:%S')
        }

@app.route('/hello')
def hello():
    return 'Hello, World!'


@app.route('/api/agent/list', methods=['GET'])
def agent_list():
    # data = request.json
    # ip = data.get('ip')
    try:
        ip = request.args.get('ip')

        if ip:
            agents = Agent.query.filter(Agent.agent_ip.like(f'%{ip}%')).all()  # 模糊查询
        else:
            agents = Agent.query.all()

        agent_list = [agent.to_dict() for agent in agents]
    except Exception as e:
        print(e)
        return JsonResponse.error()

    return JsonResponse.success(data=agent_list)


@app.route('/api/agent/add', methods=['POST'])
def agent_add():
    """
    agent 注册
    agent to gateway
    """
    # identifier = request.args.get('identifier')
    # ip = request.args.get('ip')
    # group = request.args.get('group')
    try:
        data = request.json
        current_time = datetime.now()
        new_agent = Agent(agent_identifier=data['identifier'], agent_ip=data['ip'], agent_group='default', agent_status='online', create_time=current_time)
        db.session.add(new_agent)
        db.session.commit()
    except Exception as e:
        print(e)
        return JsonResponse.error()

    return JsonResponse.success()


@app.route('/api/agent/delete', methods=['GET'])
def agent_delete():
    """
    agent 注销
    gateway to agent
    """
    # data = request.json
    # id = data['id']
    try:
        id = request.args.get('id')
        agent = Agent.query.get(id)
        agent_dict = agent.to_dict()
        ip = agent_dict['agent_ip']
        port = 9900
        # 通知 agent
        url = f"http://{ip}:{AGENT_PORT}{DEREGISTER_URL}"
        print('---deregister url: ', url)
        response = requests.post(url, json={})
        print(response.json())
        result = response.json()
        code = result['code']
        if code == 200:
            agent.agent_status = 'offline'
            db.session.commit()
        else:
            print(f'{ip} 注销失败...')
            return JsonResponse.error()
    except Exception as e:
        print(e)
        return JsonResponse.error()

    return JsonResponse.success()


@app.route('/api/agent/collect/control', methods=['POST'])
def agent_pc():
    """
    控制 agent 采集策略启停
    """
    try:
        data = request.json

        agents = Agent.query.all()
        agent_list = [agent.to_dict() for agent in agents]
        threading.Thread(target=collect_control, args=(agent_list,data,)).start()
    except Exception as e:
        print(e)
        return JsonResponse.error()

    return JsonResponse.success()


def collect_control(agents, data):
    log = data['log']
    environment = data['environment']
    network = data['network']
    trigger_task = []
    stop_task = []
    if log:
        trigger_task.append("log")
    else:
        stop_task.append("log")
    if environment:
        trigger_task.append("environment")
    else:
        stop_task.append("environment")
    if network:
        trigger_task.append("network")
    else:
        stop_task.append("network")
    param = {
        "trigger_task": ','.join(trigger_task),
        "stop_task": ','.join(stop_task)
    }
    for item in agents:
        try:
            ip = item['agent_ip']
            port = 9900
            # 通知 agent
            url = f"http://{ip}:{AGENT_PORT}{COLLECT_CONTROL_URL}"
            print('---collect control url: ', url)
            response = requests.post(url, json=param)
            # print(response.json())
            result = response.json()
            code = result['code']
            if code == 200:
                print(f'{ip} 采集启停下发成功...')
            else:
                print(f'{ip} 采集启停下发失败...')
        except Exception as e:
            print(e)


@app.route('/api/agent/policy/issue', methods=['POST'])
def agent_pi():
    """
    接收治理侧策略，下发所有 agent
    """
    try:
        data = request.json
        policy = data['policy']

        # policy to agent
        agents = Agent.query.all()
        agent_list = [agent.to_dict() for agent in agents]
        threading.Thread(target=policy_issued, args=(agent_list,)).start()
    except Exception as e:
        print(e)
        return JsonResponse.error()

    return JsonResponse.success()


def policy_issued(agents):
    for item in agents:
        try:
            ip = item['agent_ip']
            port = 9900
            # 通知 agent
            url = f"http://{ip}:{AGENT_PORT}{POLICY_RECEIVE_URL}"
            print('---policy receive url: ', url)
            response = requests.post(url, json={})
            # print(response.json())
            result = response.json()
            code = result['code']
            if code == 200:
                print(f'{ip} 策略下发成功...')
            else:
                print(f'{ip} 策略下发失败...')
        except Exception as e:
            print(e)


# @app.route('/api/agent/get', methods=['GET'])
# def agent_get():
#     agent_id = request.args.get('agent_id')
#     agent = Agent.query.get(int(agent_id))
#     agent_dict = agent.to_dict()

#     return JsonResponse.success(data=agent_dict)


# @app.route('/api/agent/update', methods=['POST'])
# def agent_update():
#     data = request.json
#     print('>>> id: ', data['id'])
#     id = request.args.get('id')
#     identifier = request.args.get('identifier')
#     ip = request.args.get('ip')
#     group = request.args.get('group')
#     current_time = datetime.now()
#     # new_agent = Agent(agent_identifier=identifier, agent_ip=ip, agent_group=group, agent_status='online', create_time=current_time)
#     new_agent = Agent(agent_identifier=data['identifier'], agent_ip=data['ip'], agent_group=data['group'], agent_status='online', create_time=current_time)
#     db.session.add(new_agent)
#     db.session.commit()

#     return JsonResponse.success()


# @app.route('/api/agent/start', methods=['GET'])
# def agent_start():
#     # data = request.json
#     # agent_id = data['agent_id']
#     agent_id = request.args.get('agent_id')
#     agent = Agent.query.get(int(agent_id))
#     agent_dict = agent.to_dict()

#     return JsonResponse.success()


# @app.route('/api/agent/stop', methods=['GET'])
# def agent_stop():
#     # data = request.json
#     # agent_id = data['agent_id']
#     agent_id = request.args.get('agent_id')
#     agent = Agent.query.get(int(agent_id))
#     agent_dict = agent.to_dict()

#     return JsonResponse.success()


@app.route('/api/logmsg/list', methods=['GET'])
def logsmsg_list():
    start_time = request.args.get('start_time')
    end_time = request.args.get('end_time')
    print('start_time: ', start_time, '; end_time: ', end_time)
    # logmsgs = LogMsg.query.all()
    logmsgs = LogMsg.query.filter(LogMsg.create_time.between(start_time, end_time)).all()
    logmsg_list = [logmsg.to_dict() for logmsg in logmsgs]

    return JsonResponse.success(data=logmsg_list)


if __name__ == '__main__':
    # app.run(debug=True)
    app.run(host='0.0.0.0', port=5000, debug=True)