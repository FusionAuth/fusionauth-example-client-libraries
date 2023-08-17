# .NET FusionAuth Kickstart Template

This [Kickstart](https://fusionauth.io/docs/v1/tech/installation-guide/kickstart) will set up a FusionAuth application and create a new user to login to the application. The Kickstart creates a new [FusionAuth Application](https://fusionauth.io/docs/v1/tech/core-concepts/applications) with the following settings:

- The Application is named `.NET Fusion`.
- The Application Client ID is `e9fdb985-9173-4e01-9d73-ac2d60d1dc8e`.
- The Client Secret is `change-this-in-production-to-be-a-real-secret`.
- The Application has a redirect URL of `https://localhost:5001/callback`.
- The Application has a logout URL of `https://localhost:5001/`.

The application is set up with the following OAuth settings:

- The OAuth Authorization Grant Types are `Authorization Code` and `Refresh Token`.
- PKCE (Proof Key for Code Exchange) is required.
- JWT (JSON Web Token) is enabled, using a newly generated asymmetric key pair (RSA).

A new user is created with the following credentials:
- Email: `dinesh@fusionauth.io`
- Password: `password`

## Prerequisites

-  Docker installed
- .NET 7.0 or later installed.

Note that this Kickstart has the following assumptions:

- You do not have a local FusionAuth instance running on port `9011`.

- Your .NET project will be running on the port `5001`. Your project might not run on the same port, as Visual Studio will randomly choose a port if the chosen one is already in use by another project. It may also be a different port if you run the project through IIS or another web server. In this case, update the `authorizedRedirectURL` and `logoutURL` variables to that of your project. If you need to, you can also update these in the Application settings page in the FusionAuth admin site in [the OAuth tab](https://fusionauth.io/docs/v1/tech/core-concepts/applications#oauth) at any time after the Kickstart has run.

- The script will create a FusionAuth application with a Client ID of `e9fdb985-9173-4e01-9d73-ac2d60d1dc8e`, and a Client Secret of `change-this-in-production-to-be-a-real-secret`. Update these as you need, but keep any sensitive secrets safe.

To use the setup script on a FusionAuth instance that does not meet these criteria, you may need to modify the script. All of the above settings can be changed in the static variables at the top of the `Program` class in the script.

## Running the KickStart

The Kickstart script can be used with a Docker Compose file to set up a local reference FusionAuth installation quickly. This folder contains a sample Docker Compose file that you can use to set up a new FusionAuth instance, with the Kickstart script running automatically. To use the Docker Compose file, run the following command in your terminal, in the same directory as the Docker Compose file:

```bash
docker-compose up
``` 

The Docker Compose file reference the Kickstart script in the `kickstart` directory. 

To shutdown and remove the Docker containers, including deleting any volumes, run the following command in the same directory as the Docker Compose file.

```bash
docker-compose down -v
```

This is useful if you want to start over with a fresh FusionAuth instance.

After running the Docker Compose file, you can access the FusionAuth admin site at [http://localhost:9011](http://localhost:9011). You can log in with the following credentials:

- Email: `dinesh@fusionauth.io`
- Password: `password`
