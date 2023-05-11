# C# Setup 

First, create a .NET 7 project in a new directory:

```sh
dotnet new console --output SetupFusionauth && cd SetupFusionauth
```

Now, copy and paste code in the `Program.cs` file into your project's `Program.cs`.

Import the neccessary NuGet packages:

```sh
dotnet add package JSON.Net
dotnet add package FusionAuth.Client 
```

```sh
dotnet publish -r osx-x64
fusionauth_api_key=<YOUR_API_KEY> bin/Debug/net7.0/osx-x64/publish/SetupFusionauth 
```