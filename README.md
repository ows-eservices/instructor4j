# Instructor4j: Ensure Structured Output from LLMs with Retries and Adaptive Prompting

<a href="https://github.com/ows-eservices/instructor4j/stargazers"><img src="https://img.shields.io/github/stars/ows-eservices/instructor4j?cacheSeconds=120" alt="Stars Badge"/></a>
<a href="https://github.com/ows-eservices/instructor4j/network/members"><img src="https://img.shields.io/github/forks/ows-eservices/instructor4j?cacheSeconds=120" alt="Forks Badge"/></a>
<a href="https://github.com/ows-eservices/instructor4j/pulls"><img src="https://img.shields.io/github/issues-pr/ows-eservices/instructor4j?cacheSeconds=120" alt="Pull Requests Badge"/></a>
<a href="https://github.com/ows-eservices/instructor4j/issues"><img src="https://img.shields.io/github/issues/ows-eservices/instructor4j?cacheSeconds=120" alt="Issues Badge"/></a>
<a href="https://github.com/ows-eservices/instructor4j/graphs/contributors"><img alt="GitHub contributors" src="https://img.shields.io/github/contributors/ows-eservices/instructor4j?color=2b9348&cacheSeconds=120"></a>
<a href="https://github.com/ows-eservices/instructor4j/blob/master/LICENSE"><img src="https://img.shields.io/github/license/ows-eservices/instructor4j?color=2b9348" alt="License Badge"/></a>


## Introduction

**Welcome!**

The **Instructor4j** is library for working with structured outputs from large language models (LLMs). It is a Java port of the **[Instructor](https://python.useinstructor.com/)** Python library made by **[Jason Liu](https://x.com/jxnlco/)**, adapting its core functionality and features for use in Java environments.

LLM function calls can sometimes fail or return incomplete results due to transient errors, network issues, or limitations in understanding the prompt. This library addresses these challenges with a robust retry mechanism and advanced prompting strategies. If a function call does not work as expected, the library automatically retries the request based on configurable settings. Additionally, it adjusts the prompt to increase the likelihood of success in subsequent attempts, ensuring more reliable and accurate responses from the language model. This combination of retries and dynamic prompt enhancement helps mitigate errors and improve overall performance.

In addition to its use with LLMs, this library can be used as a standalone function, providing structured data output based on arbitrary information expressed in natural language.

For example, given the inputs:
```text 
Nenad Alajbegovic is 30 years old
Nenad Alajbegovic will be 31 years old at this time next year 
Nenad Alajbegovic wird in vier Jahren 34 Jahre alt sein
```

You will receive a plain Java object representing a User instance with the name and age, formatted as follows in JSON:
```json
{
  "age": 30,
  "name": "Nenad Alajbegovic"
}
```

### Installation
The latest release is
[0.1.3](https://github.com/ows-eservices/instructor4j/releases/tag/v0.1.3).

It is available in Maven Central as
[solutions.own.instructor4j:instructor4j:0.1.3](http://search.maven.org/#artifactdetails%7Csolutions.own.instructor4j%7Cinstructor4j-openai%7C0.1.3%7Cjar):

Add Dependency to **pom.xml**:

```xml
<dependency>
    <groupId>solutions.own.instructor4j</groupId>
    <artifactId>instructor4j-openai</artifactId>
    <version>0.1.3</version>
</dependency>
```
or in **build.gradle**:
```groovy
dependencies {
  implementation 'solutions.own.instructor4j:instructor4j-openai:0.1.3'
}
```

### Usage
To use the Instructor4j library in your project, follow the steps below.

1. Set your **OpenAI API key** environment variable

```bash
export OPENAI_API_KEY=<apikey>
```

2. **Define Your Model Classes**  
Create model classes representing the expected structured response. Use the `@Description` annotation to add descriptions to fields.

Example:
```java
package com.example.model;

import solutions.own.instructor4j.annotation.Description;

public class User {
    @Description("The age of the user")
    private int age;

    @Description("The name of the user")
    private String name;
    
    public User() {
    }

    public User(int age, String name) {
        this.age = age;
        this.name = name;
    }

    // ... getters, setters 
}
```
3. **Use the Instructor Class**  
Here's an example of how to use the Instructor class to get a structured response from the OpenAI API.

```java
package com.example;

import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.service.impl.OpenAiChatService;
import solutions.own.instructor4j.Instructor;

import com.theokanning.openai.completion.chat.ChatMessage;

import com.example.model.User;

import java.util.List;

public class Main {
    public static void main(String[] args) {
       String apiKey = System.getenv("OPENAI_API_KEY");
       AiChatService openAiService = new OpenAiChatService(apiKey);
       Instructor instructor = new Instructor(openAiService, 3);
   
       List<ChatMessage> messages = List.of(
           new ChatMessage("user", "Nenad Alajbegovic is 30 years old")
       );
   
       try {            
            User user = instructor.createChatCompletion(messages, "gpt-3.5-turbo", User.class);
            System.out.println(user);
       } catch (InstructorException e) {
            e.printStackTrace();
       }
    }
}
```
## Code Examples
Please see examples of how Instructor4j can be used in **[instructor4j-examples](https://github.com/ows-eservices/instructor4j-examples)** repo.

## What's supported?
This version supports only **OpenAI** but can be easily adapted to use other LLM models. Support for additional LLMs will be added in the future.

## Features and Capabilities
### Generic Parameterization
The Instructor class is generic and works with any model class that represents the expected response structure.
Uses reflection to dynamically generate the JSON schema based on the provided model class.

### Validation and Error Handling
Validates the response from OpenAI against the expected model structure.
Automatically retries with adjusted prompts if validation fails.
Throws a custom InstructorException if unable to get a valid response after retries.
Provides detailed error messages and logging.

### Custom Prompting
Adjusts prompts dynamically based on missing or invalid fields.
Uses advanced prompt engineering techniques to guide the model towards producing the desired structured output.

### Additional Features
Automatic retries with adjustable maximum retry count.
Detailed logging capabilities using Java's built-in Logger.
Supports adding descriptions to model fields using the `@Description` annotation, which enhances the OpenAI function definitions.

### Logging Configuration
The Instructor class uses Java's built-in Logger.
You can configure the logging level and handlers as per your application's requirements.

### Thread Safety
The current implementation of Instructor4j is not thread-safe.
If you plan to use it in a multi-threaded environment, consider synchronizing access to shared resources or creating separate instances per thread.

### Extensibility
Custom Validation: You can extend the validateResponse method to include custom validation logic, such as value ranges or pattern matching.
Additional Models: You can create additional model classes with different structures and use them with the Instructor class.

## Conclusion
The Instructor4j library provides a robust and flexible way to obtain structured outputs from OpenAI models using Java. By leveraging OpenAI Functions and dynamic schema generation, it ensures that the responses adhere to the expected structure, reducing the need for manual parsing and error handling.

## Get Help
Please use [GitHub discussions](https://github.com/ows-eservices/instructor4j/discussions)
to get help.

## Request Features
Please let us know what features you need by [opening an issue](https://github.com/ows-eservices/instructor4j/issues/new/choose).

## License
Published under the Apache 2.0 License

## Contribute
Contribution guidelines can be found [here](https://github.com/ows-eservices/instructor4j/blob/master/CONTRIBUTING.md).
