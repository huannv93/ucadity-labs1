[
  {
    "name": "application",
    "image": "${image_repo}:${image_tag}",
    "portMappings": [
      {
        "containerPort": 8080,
        "hostPort": 0
      }
    ],
    "cpu": ${app_cpu},
    "memory": ${app_mem},
    "environment": [
      {
        "name": "DD_AGENT_HOST",
        "value": "172.17.0.1"
      },
      {
        "name": "GEAR_CDN_URL",
        "value": "${gear_cdn_url}"
      },
      {
        "name": "GEAR_DEBUG",
        "value": "${gear_debug}"
      },
      {
        "name": "GEAR_DYNAMO_PREFIX",
        "value": "${gear_dynamo_prefix}"
      },
      {
        "name": "GEAR_DYNAMO_REGION",
        "value": "${gear_dynamo_region}"
      },
      {
        "name": "GEAR_FIREBASE_CREDENTIALS",
        "value": "${gear_firebase_credentials}"
      },
      {
        "name": "GEAR_WORKER_AMOUNT",
        "value": "${gear_worker_amount}"
      },
      {
        "name": "GEAR_WORKER_SERVICE",
        "value": "${gear_worker_service}"
      },
      {
        "name": "GEAR_WORKER_VERSION",
        "value": "${gear_worker_version}"
      },
      {
        "name": "GEAR_PUSHER_URL",
        "value": "${gear_pusher_url}"
      },
      {
        "name": "GEAR_REDIS_DEFAULT",
        "value": "${gear_redis_default}"
      },
      {
        "name": "JAVA_TOOL_OPTIONS",
        "value": "-XX:MaxRAMPercentage=90.0"
      }
    ]
  }
]