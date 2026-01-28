# Java 面向对象编程学习笔记

## 1. 面向对象的基本概念

### 1.1 什么是面向对象

面向对象编程（Object-Oriented Programming，OOP）是一种编程范式，它使用"对象"来设计软件。

### 1.2 三大特性

#### 封装 (Encapsulation)
将数据和操作数据的方法封装在一个类中，隐藏内部实现细节。

```java
public class Student {
    private String name;
    private int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

#### 继承 (Inheritance)
子类可以继承父类的属性和方法，实现代码复用。

```java
// 父类
class Animal {
    public void eat() {
        System.out.println("动物在吃东西");
    }
}

// 子类
class Dog extends Animal {
    public void bark() {
        System.out.println("狗在叫");
    }
}
```

#### 多态 (Polymorphism)
同一个接口可以有多种不同的实现方式。

```java
interface Shape {
    double area();
}

class Circle implements Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

class Rectangle implements Shape {
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}
```

## 2. 类与对象

### 2.1 类的定义

类是对象的蓝图，定义了对象的属性和方法。

```java
public class Car {
    // 属性
    private String brand;
    private String model;
    private int year;

    // 构造方法
    public Car(String brand, String model, int year) {
        this.brand = brand;
        this.model = model;
        this.year = year;
    }

    // 方法
    public void start() {
        System.out.println(brand + " " + model + " 启动了");
    }

    public void stop() {
        System.out.println(brand + " " + model + " 停止了");
    }

    // Getter 和 Setter
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
```

### 2.2 对象的创建和使用

```java
public class Main {
    public static void main(String[] args) {
        // 创建对象
        Car myCar = new Car("Toyota", "Camry", 2023);

        // 使用对象
        myCar.start();
        System.out.println("品牌: " + myCar.getBrand());
        myCar.stop();
    }
}
```

## 3. 访问修饰符

Java 提供了四种访问修饰符来控制类、方法和变量的访问权限：

| 修饰符 | 同一类 | 同一包 | 子类 | 全局 |
|--------|--------|--------|------|------|
| private | ✓ | ✗ | ✗ | ✗ |
| default | ✓ | ✓ | ✗ | ✗ |
| protected | ✓ | ✓ | ✓ | ✗ |
| public | ✓ | ✓ | ✓ | ✓ |

## 4. 抽象类和接口

### 4.1 抽象类

抽象类不能被实例化，只能被继承。

```java
abstract class Animal {
    // 抽象方法，没有方法体
    public abstract void makeSound();

    // 普通方法
    public void sleep() {
        System.out.println("动物在睡觉");
    }
}

class Cat extends Animal {
    @Override
    public void makeSound() {
        System.out.println("喵喵喵");
    }
}
```

### 4.2 接口

接口定义了一组方法，实现类必须实现这些方法。

```java
interface Flyable {
    void fly();
    void land();
}

interface Swimmable {
    void swim();
}

class Duck implements Flyable, Swimmable {
    @Override
    public void fly() {
        System.out.println("鸭子在飞");
    }

    @Override
    public void land() {
        System.out.println("鸭子着陆");
    }

    @Override
    public void swim() {
        System.out.println("鸭子在游泳");
    }
}
```

## 5. 内部类

### 5.1 成员内部类

```java
class Outer {
    private int outerField = 10;

    class Inner {
        public void display() {
            System.out.println("外部类字段: " + outerField);
        }
    }

    public void createInner() {
        Inner inner = new Inner();
        inner.display();
    }
}
```

### 5.2 静态内部类

```java
class Outer {
    private static int staticField = 20;

    static class StaticInner {
        public void display() {
            System.out.println("静态字段: " + staticField);
        }
    }
}
```

### 5.3 匿名内部类

```java
interface Greeting {
    void greet();
}

public class Main {
    public static void main(String[] args) {
        Greeting greeting = new Greeting() {
            @Override
            public void greet() {
                System.out.println("Hello, World!");
            }
        };
        greeting.greet();
    }
}
```

## 6. 常用设计模式

### 6.1 单例模式

```java
public class Singleton {
    private static Singleton instance;

    private Singleton() {
        // 私有构造方法
    }

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

### 6.2 工厂模式

```java
interface Shape {
    void draw();
}

class Circle implements Shape {
    @Override
    public void draw() {
        System.out.println("画圆");
    }
}

class Rectangle implements Shape {
    @Override
    public void draw() {
        System.out.println("画矩形");
    }
}

class ShapeFactory {
    public Shape createShape(String type) {
        if ("circle".equalsIgnoreCase(type)) {
            return new Circle();
        } else if ("rectangle".equalsIgnoreCase(type)) {
            return new Rectangle();
        }
        return null;
    }
}
```

## 7. 总结

面向对象编程的核心思想是将现实世界的事物抽象为对象，通过对象之间的交互来构建软件系统。掌握好面向对象的三大特性（封装、继承、多态）是学习 Java 的关键。

### 学习要点
- ✅ 理解类和对象的概念
- ✅ 掌握访问修饰符的使用
- ✅ 熟悉抽象类和接口的区别
- ✅ 了解常用的设计模式
- ✅ 多写代码，多实践

> **提示**：面向对象编程不仅仅是语法，更是一种思维方式。在实际开发中，要根据具体需求选择合适的编程范式。