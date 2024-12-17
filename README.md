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
[2.0.0](https://github.com/ows-eservices/instructor4j/releases/tag/v2.0.0).

It is available in Maven Central as
[solutions.own.instructor4j:instructor4j:2.0.0](http://search.maven.org/#artifactdetails%7Csolutions.own.instructor4j%7Cinstructor4j-openai%7C2.0.0%7Cjar):

Add Dependency to **pom.xml**:

```xml
<dependency>
    <groupId>solutions.own.instructor4j</groupId>
    <artifactId>instructor4j-openai</artifactId>
    <version>2.0.0</version>
</dependency>
```
or in **build.gradle**:
```groovy
dependencies {
  implementation 'solutions.own.instructor4j:instructor4j-openai:2.0.0'
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

import jakarta.validation.constraints.NotNull;
import solutions.own.instructor4j.annotation.Description;

public class User {
    @Description("The age of the user")
    @NotNull
    private int age;

    @Description("The name of the user")
    @NotNull
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
import solutions.own.instructor4j.model.BaseMessage;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.service.impl.OpenAiChatService;
import solutions.own.instructor4j.Instructor;

import com.example.model.User;

import java.util.List;

public class Main {
    public static void main(String[] args) {
       String apiKey = System.getenv("OPENAI_API_KEY");
       AiChatService openAiService = new OpenAiChatService(apiKey);
       Instructor instructor = new Instructor(openAiService, 3);

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), "Nenad Alajbegovic is 30 years old")
        ));
   
       try {            
            User user = instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);
            System.out.println(user);
       } catch (InstructorException e) {
            e.printStackTrace();
       }
    }
}
```

### Streaming
Instructor4j supports partial streaming completions, allowing you to receive extracted data in real-time as the model generates its response. This can be useful for providing a more interactive user experience or processing large amounts of data incrementally.

Example:
```java
package com.example;

import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.model.BaseMessage;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.service.impl.OpenAiChatService;
import solutions.own.instructor4j.Instructor;
import solutions.own.instructor4j.util.Utils;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.model.ConferenceParticipant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    
    private final static String meetingRecord = "In our recent online meeting, participants from various backgrounds joined to discuss the upcoming tech conference. " +
        "The names and contact details of the participants were as follows:\n" +
        "\n" +
        "- Name: John Doe, Email: johndoe@email.com, Twitter: @TechGuru44\n" +
        "- Name: Jane Smith, Email: janesmith@email.com, Twitter: @DigitalDiva88\n" +
        "- Name: Alex Johnson, Email: alexj@email.com, Twitter: @CodeMaster2023";
    
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        AiChatService openAiService = new OpenAiChatService(apiKey);
        Instructor instructor = new Instructor(openAiService, 3);

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), meetingRecord)
        ));

        Flux<String> extractionStream = 
            instructor.createStreamChatCompletion(messages, "gpt-4o-mini", ConferenceParticipant.class);

        extractionStream
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(extraction -> {
                if (extraction != null) {
                    // Let us assure that json received always have balanced quotes, curly braces, and square brackets
                    String consistentJson = Utils.ensureJsonClosures(fullResponseReceived.toString());
                    JsonNode rootNode;
                    try {
                        rootNode = objectMapper.readTree(consistentJson);
                        JsonNode dataNode = rootNode.get("data");
                        List<ConferenceParticipant> participants = objectMapper.convertValue(dataNode, new TypeReference<List<ConferenceParticipant>>() {});
                        if (participants != null) {
                            for (ConferenceParticipant p : participants) {
                                System.out.println(
                                    "    PARTIAL DATA RECEIVED: " + p.getName() + ", " + p.getEmail() + ", "
                                        + p.getTwitter());
                            }
                        }
                    } catch (JsonProcessingException e) {
                    }
                }
            })
            .doOnError(error -> {
                System.out.println("Flux emitted an unexpected error: " + error.getMessage());
            })
            .doOnComplete(() -> {
                // ...
            })
            .blockLast();
    }
}
```

The extractionStream variable holds an async generator that yields partial extraction results as they become available. We iterate over the stream updating the extraction object with each partial result and logging it to the console.
In order to have valid JSON structure we assure that json received always have balanced quotes, curly braces, and square brackets.

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

### Thread Safety
The current implementation of Instructor4j is not thread-safe.
If you plan to use it in a multi-threaded environment, consider synchronizing access to shared resources or creating separate instances per thread.

## Conclusion
The Instructor4j library provides a robust and flexible way to obtain structured outputs from OpenAI models using Java. By leveraging OpenAI Functions and dynamic schema generation, it ensures that the responses adhere to the expected structure, reducing the need for manual parsing and error handling. Instructor4j supports partial streaming completions, allowing you to receive extracted data in real-time as the model generates its response.

## Get Help
Please use [GitHub discussions](https://github.com/ows-eservices/instructor4j/discussions)
to get help.

## Request Features
Please let us know what features you need by [opening an issue](https://github.com/ows-eservices/instructor4j/issues/new/choose).

## License
Published under the Apache 2.0 License

## Contribute
Contribution guidelines can be found [here](https://github.com/ows-eservices/instructor4j/blob/master/CONTRIBUTING.md).
