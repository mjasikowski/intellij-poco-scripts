# Datagrip POCO Groovy scripts
A set of Groovy scripts for DataGrip to export tables to C# classes for Dapper and TypeScript interfaces. Tested on PostgreSQL and MS SQL.

# How to install
Copy the contents of this repository to the DataGrip scripts directory - on Windows that's most likely C:\Users\_username_\.DataGrip2019.2\config\extensions\com.intellij.database\schema

# How to use

Right click on your table and find the scripts in the "scripted extensions" context menu:

![image](https://user-images.githubusercontent.com/4477538/81436167-4eed7580-9169-11ea-83bb-04815dd887ad.png)

# Example output

## Table

![image](https://user-images.githubusercontent.com/4477538/81435312-ef429a80-9167-11ea-9d94-732ef471827e.png)

## C#

```c#
public class User
{
    [Key]
    public Guid UserId { get; set; }
    public Guid RoleId { get; set; }
    public string UserLogin { get; set; }
    public string PasswordHash { get; set; }
    public string EmailAddress { get; set; }
    public string FirstName { get; set; }
    public string LastName { get; set; }
    public int? Testnumber { get; set; }
}
```

## Typescript

```typescript
export default interface User {
    userId: string
    roleId: string
    userLogin: string
    passwordHash: string
    emailAddress: string
    firstName: string
    lastName: string
    testnumber?: number
}
```

# License
You are licensed to do whatever the hell you want with those scripts, though it would be nice if you'd credit me if you use it.
