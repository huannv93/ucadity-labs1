aws ecs register-task-definition --region ap-northeast-1 --cli-input-json '{
  "taskDefinition": {
    "taskDefinitionArn": "arn:aws:ecs:ap-northeast-1:221008696644:task-definition/CloudSM:6",
    "containerDefinitions": [
      {
        "name": "CloudSM",
        "image": "221008696644.dkr.ecr.ap-northeast-1.amazonaws.com/huannv-repo-private:2356a4f",
        "cpu": 1024,
        "memory": 2048,
        "portMappings": [
          {
            "containerPort": 3030,
            "hostPort": 3030,
            "protocol": "tcp"
          }
        ],
        "essential": true,
        "environment": [
          {
            "name": "TYPEORM_HOST",
            "value": "dbcluster01.cluster-cyi2msewkr3u.ap-northeast-1.rds.amazonaws.com"
          },
          {
            "name": "TYPEORM_PASSWORD",
            "value": "nguyenvanhuan_fpt"
          },
          {
            "name": "TYPEORM_DATABASE",
            "value": "udacity"
          },
          {
            "name": "TYPEORM_PORT",
            "value": "5432"
          },
          {
            "name": "TYPEORM_USERNAME",
            "value": "testuser"
          }
        ],
        "mountPoints": [],
        "volumesFrom": [],
        "logConfiguration": {
          "logDriver": "awslogs",
          "options": {
            "awslogs-group": "/ecs/CloudSM",
            "awslogs-region": "ap-northeast-1",
            "awslogs-stream-prefix": "ecs"
          }
        }
      }
    ],
    "family": "CloudSM",
    "taskRoleArn": "arn:aws:iam::221008696644:role/ecs-task-role",
    "executionRoleArn": "arn:aws:iam::221008696644:role/ecs-task-role",
    "networkMode": "awsvpc",
    "revision": 6,
    "volumes": [],
    "status": "ACTIVE",
    "requiresAttributes": [
      {
        "name": "com.amazonaws.ecs.capability.logging-driver.awslogs"
      },
      {
        "name": "ecs.capability.execution-role-awslogs"
      },
      {
        "name": "com.amazonaws.ecs.capability.ecr-auth"
      },
      {
        "name": "com.amazonaws.ecs.capability.docker-remote-api.1.19"
      },
      {
        "name": "com.amazonaws.ecs.capability.task-iam-role"
      },
      {
        "name": "ecs.capability.execution-role-ecr-pull"
      },
      {
        "name": "com.amazonaws.ecs.capability.docker-remote-api.1.18"
      },
      {
        "name": "ecs.capability.task-eni"
      }
    ],
    "placementConstraints": [],
    "compatibilities": [
      "EC2",
      "FARGATE"
    ],
    "requiresCompatibilities": [
      "FARGATE"
    ],
    "cpu": "1024",
    "memory": "2048",
    "registeredAt": "2023-11-09T06:50:46.175000+00:00",
    "registeredBy": "arn:aws:iam::221008696644:user/HuanNV10"
  },
  "tags": []
}'



aws ecs register-task-definition --family CloudSM --container-definitions '[{"name":"CloudSM","image":"221008696644.dkr.ecr.ap-northeast-1.amazonaws.com/huannv-repo-private:2356a4f","cpu":1024,"memory":2048,"portMappings":[{"containerPort":3030,"hostPort":3030,"protocol":"tcp"}],"essential":true,"environment":[{"name":"TYPEORM_HOST","value":"dbcluster01.cluster-cyi2msewkr3u.ap-northeast-1.rds.amazonaws.com"},{"name":"TYPEORM_PASSWORD","value":"nguyenvanhuan_fpt"},{"name":"TYPEORM_DATABASE","value":"udacity"},{"name":"TYPEORM_PORT","value":"5432"},{"name":"TYPEORM_USERNAME","value":"testuser"}],"mountPoints":[],"volumesFrom":[],"logConfiguration":{"logDriver":"awslogs","options":{"awslogs-group":"/ecs/CloudSM","awslogs-region":"ap-northeast-1","awslogs-stream-prefix":"ecs"}}}]' --requires-compatibilities FARGATE --network-mode awsvpc --region ap-northeast-1

TASK_DEFINITION=$(aws ecs describe-task-definition — task-definition ${TASK_DEFINITION_NAME} )

aws ecs register-task-definition --family ${TASK_DEFINITION_NAME} --region ap-northeast-1 --cli-input-json file://task-def.json




TASK_DEFINITION_NAME=CloudSM

TASK_DEFINITION=$(aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_NAME} --region ap-northeast-1 )

echo ${TASK_DEFINITION} | jq --arg newImage "${NEW_ECR_IMAGE}" '.containerDefinitions[0].image=$newImage' > task-def.json

aws ecs register-task-definition --family ${TASK_DEFINITION_NAME} --region ap-northeast-1 --cli-input-json file://task-def.json

aws ecs register-task-definition --family CloudSM --container-definitions '${container_definitions}' --compatibilities FARGATE --region ${AWS_DEFAULT_REGION}


aws ecs update-service --region ap-northeast-1  --cluster ecs-cluster-CloudSM --service ecs-svc-CloudSM --task-definition CloudSM:12


aws ecs update-service --region ap-northeast-1  --cluster ecs-cluster-CloudSM --service ecs-svc-CloudSM --task-definition CloudSM:12 --force-new-deployment