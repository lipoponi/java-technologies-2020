package ru.ifmo.rain.tebloev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates implementation for provided token.
 *
 * @author Stanislav Tebloev
 */
class CodeGenerator {
    /**
     * Returns default value for specified token
     *
     * @param token class token
     * @return default value for specified token
     * @throws ImplerException if no default value defined
     */
    private String getClassDefaultValue(final Class<?> token) throws ImplerException {
        if (token.equals(void.class)) {
            throw new ImplerException("void cannot have default value");
        }

        if (token.isPrimitive()) {
            return token.equals(boolean.class) ? "false" : "0";
        } else {
            return "null";
        }
    }

    /**
     * Returns raw name for type
     *
     * @param type specified {@link Type} object
     * @return raw name for type
     */
    private String getRawTypeName(Type type) {
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }

        return type.getTypeName();
    }

    /**
     * Returns complete generic type string for specified {@link Type} object
     *
     * @param type specified {@link Type} object
     * @return complete generic type string for specified {@link Type} object
     */
    private String getGenericString(final Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;

            String rawTypeName = getRawTypeName(parameterized);

            if (parameterized.getOwnerType() != null) {
                Type owner = parameterized.getOwnerType();
                int rawPrefixLength = getRawTypeName(owner).length() + 1;

                rawTypeName = String.format("%s.%s", getGenericString(owner), rawTypeName.substring(rawPrefixLength));
            }

            String params = getTypeParameters(parameterized.getActualTypeArguments());

            return String.format("%s%s", rawTypeName, params);
        }

        if (type instanceof Class<?>) {
            Class<?> token = (Class<?>) type;
            String params = getTypeParameters(token.getTypeParameters());

            return String.format("%s%s", token.getCanonicalName(), params);
        }

        return type.getTypeName();
    }

    /**
     * Returns generic parameter list string. If {@code parameters} is empty, blank string is returned.
     *
     * @param parameters array containig {@link Type} objects
     * @return generic parameter list
     */
    private String getTypeParameters(final Type[] parameters) {
        return parameters.length == 0
                ? ""
                : Arrays.stream(parameters)
                .map(this::getGenericString)
                .collect(Collectors.joining(", ", "<", ">"));
    }

    /**
     * Generates type variable declaration string. If {@code variables} is empty, blank string is returned.
     *
     * @param variables array containing {@link TypeVariable} objects
     * @return string declaration of type variables
     */
    private String getTypeParameterDeclaration(final TypeVariable<?>[] variables) {
        return variables.length == 0
                ? ""
                : Arrays.stream(variables)
                .map(var -> {
                    StringBuilder result = new StringBuilder(var.getName());
                    Type[] bounds = var.getBounds();

                    if (bounds.length != 0) {
                        result.append(" extends ");
                        result.append(Arrays.stream(bounds)
                                .map(this::getGenericString)
                                .collect(Collectors.joining(" & ")));
                    }

                    return result;
                })
                .collect(Collectors.joining(", ", "<", ">"));
    }

    /**
     * Generates definition for specified executable.
     *
     * @param executable class method or constructor
     * @param returnType return type of {@code executable}
     * @param name       the name of {@code executable}
     * @return definition string for {@code executable}
     */
    private String getExecutableSignature(final Executable executable, Type returnType, final String name) {
        String typeParams = getTypeParameterDeclaration(executable.getTypeParameters());
        String paramList = Arrays.stream(executable.getParameters())
                .map(parameter -> getGenericString(parameter.getParameterizedType()) + " " + parameter.getName())
                .collect(Collectors.joining(", ", "(", ")"));
        String throwList = Arrays.stream(executable.getGenericExceptionTypes())
                .map(this::getGenericString)
                .collect(Collectors.joining(", "));

        StringBuilder result = new StringBuilder();
        if (!typeParams.isEmpty()) {
            result.append(typeParams).append(" ");
        }
        if (returnType != null) {
            result.append(getGenericString(returnType)).append(" ");
        }
        result.append(name).append(paramList);
        if (!throwList.isEmpty()) {
            result.append(" throws ").append(throwList);
        }

        return result.toString();
    }

    /**
     * Generates definition for specified constructor.
     *
     * @param constructor {@link Constructor} object
     * @return definition string for {@code constructor}
     */
    private String getConstructor(final Constructor<?> constructor) {
        String superCall = Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.joining(", ", "super(", ");"));
        String constructorName = getImplName(constructor.getDeclaringClass());

        return String.format("public %s { %s }", getExecutableSignature(constructor, null, constructorName), superCall);
    }

    /**
     * Generates definition for specified method.
     *
     * @param method {@link Method} object
     * @return definition string for {@code method}
     * @throws ImplerException if no default value for {@code returnType} defined
     */
    private String getMethod(final Method method) throws ImplerException {
        Class<?> returnToken = method.getReturnType();
        Type returnType = method.getGenericReturnType();

        String body = returnToken.equals(void.class)
                ? " "
                : String.format(" return %s; ", getClassDefaultValue(returnToken));

        return String.format("public %s {%s}", getExecutableSignature(method, returnType, method.getName()), body);
    }

    /**
     * Generates implementation name for specified base token.
     *
     * @param token {@link Class} object of base
     * @return implementation name string
     */
    private String getImplName(final Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Returns one of existing constructors of specified token. If no constructors were found null is returned.
     *
     * @param token {@link Class} object
     * @return constructor of {@code token} or {@code null}
     */
    private Constructor<?> getDefaultConstructor(final Class<?> token) {
        for (final Constructor<?> constructor : token.getDeclaredConstructors()) {
            int mod = constructor.getModifiers();
            if (!Modifier.isPrivate(mod)) {
                return constructor;
            }
        }

        return null;
    }

    /**
     * Returns {@link List} of all abstract {@link Method} needed to implement in derived from token class.
     *
     * @param token base class or interface
     * @return {@link List} of {@link Method} to implement
     */
    private List<Method> getMethodsToImplement(final Class<?> token) {
        List<Class<?>> list = new ArrayList<>();
        list.add(token);

        Set<Class<?>> visited = new HashSet<>();

        for (int i = 0; i < list.size(); i++) {
            Class<?> current = list.get(i);
            for (Class<?> interfaceToken : current.getInterfaces()) {
                if (!visited.contains(interfaceToken)) {
                    list.add(interfaceToken);
                    visited.add(interfaceToken);
                }
            }

            Class<?> base = current.getSuperclass();
            if (base != null) {
                list.add(base);
                visited.add(base);
            }
        }

        List<Method> result = new ArrayList<>();

        for (final Class<?> current : list) {
            Arrays.stream(current.getDeclaredMethods())
                    .filter(method -> {
                        int mod = method.getModifiers();
                        return !Modifier.isPrivate(mod)
                                && (Modifier.isPublic(mod)
                                || Modifier.isProtected(mod)
                                || method.getDeclaringClass().getPackage().equals(token.getPackage()));
                    })
                    .collect(Collectors.toCollection(() -> result));
        }

        Collection<Method> distinct = result.stream()
                .collect(Collectors.toMap(
                        method -> method.getName() + Arrays.toString(method.getParameterTypes()),
                        method -> method,
                        (lhs, rhs) -> lhs))
                .values();

        return distinct.stream()
                .filter(method -> {
                    int mod = method.getModifiers();
                    return Modifier.isAbstract(mod);
                }).collect(Collectors.toList());
    }

    /**
     * Generates whole file of implementation and writes it to provided {@link Writer}.
     *
     * @param writer {@link Writer} to write
     * @param token  {@link Class} token of base class or interface
     * @throws ImplerException if no implementation can be created
     */
    public void writeTokenImplementation(final Writer writer, final Class<?> token) throws ImplerException {
        int modifiers = token.getModifiers();

        if (Modifier.isPrivate(modifiers)) {
            throw new ImplerException("cannot implement private token");
        }

        if (Modifier.isFinal(modifiers)) {
            throw new ImplerException("base class cannot be final");
        }

        if (token.isArray()) {
            throw new ImplerException("cannot inherit from array");
        }

        if (token.isEnum() || token.equals(Enum.class)) {
            throw new ImplerException("cannot inherit from enum");
        }

        try {
            if (!token.getPackageName().isEmpty()) {
                writer.write(String.format("package %s;%n%n", token.getPackageName()));
            }

            String typeParameters = getTypeParameterDeclaration(token.getTypeParameters());
            writer.append("public class ").append(getImplName(token)).append(typeParameters).append(" ");
            writer.append(Modifier.isInterface(modifiers) ? "implements" : "extends").append(" ");
            writer.append(getGenericString(token)).append(" {");

            if (!Modifier.isInterface(modifiers)) {
                Constructor<?> constructor = getDefaultConstructor(token);
                if (constructor == null) {
                    throw new ImplerException("no accessible constructor");
                }

                writer.write(String.format("%n    %s%n", getConstructor(constructor)));
            }

            for (Method method : getMethodsToImplement(token)) {
                if (Modifier.isAbstract(method.getModifiers())) {
                    writer.write(String.format("%n    %s%n", getMethod(method)));
                }
            }

            writer.append("}").append(System.lineSeparator());
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }
}
