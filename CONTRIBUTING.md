Thank you for investing your time and effort in contributing to our project, we appreciate it a lot! 🤗

# General guidelines

- If you want to contribute a bug fix or a new feature that isn't listed in the [issues](https://github.com/ows-eservices/instructor4j/issues) yet, please open a new issue for it. We will prioritize is shortly.
- Follow [Google's Best Practices for Java Libraries](https://jlbp.dev/)
- Keep the code compatible with Java 17.
- Avoid adding new dependencies as much as possible (new dependencies with test scope are OK). If absolutely necessary, try to use the same libraries which are already used in the project.
- Write unit and/or integration tests for your code. This is critical: no tests, no review!
- Avoid making breaking changes. Always keep backward compatibility in mind. For example, instead of removing fields/methods/etc, mark them `@Deprecated` and make sure they still work as before.
- Follow existing naming conventions.
- Avoid using Lombok in the new code, and remove it from the old code if you get a chance.
- Add Javadoc where necessary. There's no need to duplicate Javadoc from the implemented interfaces.
- Follow existing code style present in the project.
- Large features should be discussed with maintainers before implementation.
