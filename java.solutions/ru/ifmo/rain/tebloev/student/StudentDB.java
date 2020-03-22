package ru.ifmo.rain.tebloev.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;


import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudentDB implements AdvancedStudentGroupQuery {
    private static final String DEFAULT_STRING = "";

    private static String getFullName(final Student student) {
        return String.format("%s %s", student.getFirstName(), student.getLastName());
    }

    private <T> List<T> getMappedStudents(Collection<Student> students, Function<Student, T> function) {
        return students.stream().map(function).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getMappedStudents(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getMappedStudents(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getMappedStudents(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getMappedStudents(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return getFirstNames(sortStudentsById(students)).stream()
                .findFirst().orElse(DEFAULT_STRING);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream().sorted(Comparator.comparing(Student::getLastName)
                .thenComparing(Student::getFirstName)
                .thenComparing(Student::getId)).collect(Collectors.toList());
    }

    private <T extends Comparable<T>>
    List<Student> filterStudentsByField(Collection<Student> students, Function<Student, T> extractor, T value) {
        return students.stream().filter(student -> extractor.apply(student).equals(value)).collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterStudentsByField(sortStudentsByName(students), Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterStudentsByField(sortStudentsByName(students), Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return filterStudentsByField(sortStudentsByName(students), Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }

    private List<Group> getGroupsSortedByName(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup)).entrySet().stream()
                .map(e -> new Group(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(Group::getName)).collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsSortedByName(sortStudentsByName(students));
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsSortedByName(sortStudentsById(students));
    }

    private String getLargestGroup(Collection<Student> students, Comparator<Group> comparator) {
        return getGroupsById(students).stream()
                .sorted(comparator.reversed())
                .map(Group::getName).findFirst().orElse(DEFAULT_STRING);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroup(students, Comparator.comparing(group -> group.getStudents().size()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroup(students, Comparator.comparing(group ->
                getDistinctFirstNames(group.getStudents()).size()
        ));
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(StudentDB::getFullName)).entrySet().stream()
                .map((Map.Entry<String, List<Student>> entry) -> {
                    return Map.entry(entry.getKey(), getGroups(entry.getValue()).stream().distinct().count());
                })
                .max(Comparator.comparing((Function<Map.Entry<String, Long>, Long>) Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey).orElse(DEFAULT_STRING);
    }

    private <T> List<T> getByIndices(T[] array, int[] indices) {
        return Arrays.stream(indices).mapToObj(idx -> array[idx]).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndices(getMappedStudents(students, Student::getFirstName).toArray(new String[0]), indices);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndices(getMappedStudents(students, Student::getLastName).toArray(new String[0]), indices);
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return getByIndices(getMappedStudents(students, Student::getGroup).toArray(new String[0]), indices);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndices(getMappedStudents(students, StudentDB::getFullName).toArray(new String[0]), indices);
    }
}
