# :beginner: Dev Service

> Dev Service is a handy service to let HIP/HIU bridge developers work autonomously by providing features
> such as manipulate bridge services entries, etc.

# Pre-requisites & Setting up the environment

 * Git (also setup SSH Key)
 * Intelli Idea IDE
   * Install Lombok Plugin in the IDE and enable Lombok Annotations in preferences

## Build Status

[![CICD](https://github.com/ProjectEKA/dev-service/workflows/Build%20and%20push%20Docker%20Image/badge.svg)](https://github.com/ProjectEKA/dev-service/actions)

## :tada: Language/Frameworks

*   [JAVA](https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/)
*   [spring webflux](https://docs.microsoft.com/en-us/aspnet/core/?view=aspnetcore-3.1)
*   [Easy Random](https://github.com/j-easy/easy-random)
*   [gradle](https://docs.gradle.org/5.6.4/userguide/userguide.html)

## :checkered_flag: Requirements

*   [docker >= 19.03.5](https://www.docker.com/)

```  
## :whale: Running From The Docker Image

Create docker image

```alpha
docker build -t dev-service .
```

To run the image

```alpha
docker run -d -p 9090:9090 dev-service
```

## :rocket: Running From Source

To run

```alpha
./gradlew bootRunLocal
```

## Running The Tests

To run the tests
```alpha
./gradlew test
```

## Setting up local machine 

Assuming [gateway](https://github.com/ProjectEKA/gateway) is up and running as well as Keycloak is running on your local,

To be able to manipulate hip/hiu services entries & update bridge endpoint, you need to add admin role to gateway client and create admin-user in `Central-Registry` realm.

  ### How to add admin role to the gateway client
    
   1. Click on `Clients`
   2. Go to the `gateway` client
   3. Click on `Roles` tab
   4. Click on `Add Role`
   5. Enter Role Name as `admin`
   6. Click `Save`
    
  ### How to create admin-user
    
   1. Click on `Users`
   2. Click on `Add user`
   3. Enter Username as `admin-user`
   4. Click `Save`
   5. Click on `Credentials` tab
   6. Set Temporary check to `OFF`
   7. Enter Password and Password Confirmation as `welcome`
   8. Click on `Set Password`
   9. Click on `Role Mappings` tab
   10. Click on `Client Roles - Select a client` search box
   11. Type `gateway` and click enter
   12. On the `Available Roles` you should see `admin` role, select that to assign, and then click `Add Selected`
   13. Type `realm-management` and click enter
   14. On the `Available Roles` you should see `manage-clients` and `manage-users` roles, select those to assign, and then click `Add Selected`
