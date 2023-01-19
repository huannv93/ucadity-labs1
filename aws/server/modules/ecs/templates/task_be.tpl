{
  "ipcMode": null,
  "executionRoleArn": null,
  "containerDefinitions": [
    {
      "dnsSearchDomains": null,
      "environmentFiles": null,
      "logConfiguration": null,
      "entryPoint": [],
      "portMappings": [
        {
          "hostPort": 3030,
          "protocol": "tcp",
          "containerPort": 3030
        }
      ],
      "command": [],
      "linuxParameters": null,
      "cpu": 0,
      "environment": [],
      "resourceRequirements": null,
      "ulimits": null,
      "dnsServers": null,
      "mountPoints": [],
      "workingDirectory": null,
      "secrets": null,
      "dockerSecurityOptions": null,
      "memory": 800,
      "memoryReservation": null,
      "volumesFrom": [],
      "stopTimeout": null,
      "image": "huannv93/udacity-awsdevops-project03-1_backend",
      "startTimeout": null,
      "firelensConfiguration": null,
      "dependsOn": null,
      "disableNetworking": null,
      "interactive": null,
      "healthCheck": null,
      "essential": true,
      "links": null,
      "hostname": null,
      "extraHosts": null,
      "pseudoTerminal": null,
      "user": null,
      "readonlyRootFilesystem": null,
      "dockerLabels": null,
      "systemControls": null,
      "privileged": null,
      "name": "be"
    }
  ],
  "placementConstraints": [],
  "memory": "1024",
  "taskRoleArn": null,
  "compatibilities": [
    "EXTERNAL",
    "EC2"
  ],
  "taskDefinitionArn": "arn:aws:ecs:us-west-2:405253367546:task-definition/be:3",
  "family": "be",
  "requiresAttributes": [],
  "pidMode": null,
  "requiresCompatibilities": [
    "EC2"
  ],
  "networkMode": null,
  "runtimePlatform": null,
  "cpu": "1024",
  "revision": 3,
  "status": "ACTIVE",
  "inferenceAccelerators": null,
  "proxyConfiguration": null,
  "volumes": []
}