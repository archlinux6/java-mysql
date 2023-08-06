// 汽车类
public class Car {
    private int Id; // 品牌
    private String brand; // 品牌
    private String model; // 型号
    private int mileage; // 里程数
    private int price;
    private String publishTime;

    public Car() {

    }

    // 构造方法
    public Car(int Id, String brand, String model, int mileage, int price, String publishTime) {
        this.Id = Id;
        this.brand = brand;
        this.model = model;
        this.mileage = mileage;
        this.price = price;
        this.publishTime = publishTime;
    }

    // getter和setter方法
    public void setModel(String model) {
        this.model = model;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getMileage() {
        return mileage;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public int getPrice() {
        return price;
    }

    public int getId() {
        return Id;
    }
}