def main(ctx):
  commit_message = ctx.build.message.lower()
  if "readme.md" in commit_message or "[no_ci]" in commit_message:
    return nullPipeline()
  
  if "[db]" in commit_message:
    return [
        ci(ctx),
        cd(ctx),
        db(ctx)
    ]
  else :
    return [
        ci(ctx),
        cd(ctx),
    ]

def nullPipeline():
  return {
    "kind": "pipeline",
    "name": "Nothing",
    "steps": []
  }

def ci(ctx):
  CI = {
    "kind": "pipeline",
    "name": "CI",
    "steps": [
      {
        "name": "compilation",
        "image": "maven:3-openjdk-11",
        "commands": [
            "cd Sources",
            "mvn clean package",
        ]
      },
      {
        "name": "code-analysis",
        "image": "openjdk:8-jdk",
        "commands": [
            "export SONAR_SCANNER_VERSION=4.7.0.2747",
            "export SONAR_SCANNER_HOME=$HOME/.sonar/sonar-scanner-$SONAR_SCANNER_VERSION-linux",
            "curl --create-dirs -sSLo $HOME/.sonar/sonar-scanner.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SONAR_SCANNER_VERSION-linux.zip",
            "unzip -o $HOME/.sonar/sonar-scanner.zip -d $HOME/.sonar/",
            "export PATH=$SONAR_SCANNER_HOME/bin:$PATH",
            "export SONAR_SCANNER_OPTS=\"-server\"",
            "sonar-scanner -D sonar.projectKey=Api-Allin -D sonar.sources=./Sources -D sonar.host.url=https://codefirst.iut.uca.fr/sonar",
        ],
        "settings": {
          "sources": "./src/",
        },
        "environment": {
          "SONAR_TOKEN": {"from_secret": "SECRET_TOKEN"},
        },
      }
    ]
  }
  return CI

def cd(ctx):
  CD = {
    "kind": "pipeline",
    "name": "CD",
    "steps": [
      {
        "name": "hadolint",
        "image": "hadolint/hadolint:latest-alpine",
        "commands": [
            "hadolint Sources/Dockerfile"
        ]
      },
      {
        "name": "docker-image",
        "image": "plugins/docker",
        "settings": {
          "dockerfile": "Sources/Dockerfile",
          "context": "Sources",
          "registry": "hub.codefirst.iut.uca.fr",
          "repo": "hub.codefirst.iut.uca.fr/lucas.evard/api",
          "username": {"from_secret": "SECRET_REGISTRY_USERNAME"},
          "password": {"from_secret": "SECRET_REGISTRY_PASSWORD"}
        }
      },
      {
        "name": "deploy-container",
        "image": "hub.codefirst.iut.uca.fr/thomas.bellembois/codefirst-dockerproxy-clientdrone:latest",
        "environment": {
          "CODEFIRST_CLIENTDRONE_ENV_DATA_SOURCE": "postgres",
          "CODEFIRST_CLIENTDRONE_ENV_CODEFIRST_CONTAINER": {"from_secret": "CODEFIRST_CONTAINER"},
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_DB": {"from_secret": "db_database"},
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_USER": {"from_secret": "db_user"},
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_PASSWORD": {"from_secret": "db_password"},
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_HOST": {"from_secret": "db_host"},
          "CODEFIRST_CLIENTDRONE_ENV_SALT": {"from_secret": "SALT"},
          "ADMINS": ["lucasevard", "emrekartal", "arthurvalin", "lucasdelanier"],
          "IMAGENAME": "hub.codefirst.iut.uca.fr/lucas.evard/api:latest",
          "CONTAINERNAME": "api",
          "COMMAND": "create",
          "OVERWRITE": "true",
        },
        "depends_on": [
          "docker-image"
        ]
      }
    ]
  }

  return CD
    
def db(ctx):
  DB = {
    "kind": "pipeline",
    "name": "DB",
    "steps": [
      {
        "name": "deploy-container-postgres",
        "image": "hub.codefirst.iut.uca.fr/thomas.bellembois/codefirst-dockerproxy-clientdrone:latest",
        "environment": {
          "IMAGENAME": "postgres:latest",
          "CONTAINERNAME": "postgresapi",
          "COMMAND": "create",
          "OVERWRITE": "false",
          "PRIVATE": "false",
          "ADMINS": ["lucasevard", "emrekartal", "arthurvalin", "lucasdelanier"],
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_ROOT_PASSWORD": {"from_secret": "db_root_password"},
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_DB": {"from_secret": "db_database"},
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_USER": {"from_secret": "db_user"},
          "CODEFIRST_CLIENTDRONE_ENV_POSTGRES_PASSWORD": {"from_secret": "db_password"}
        }
      }
    ]
  }
  return DB