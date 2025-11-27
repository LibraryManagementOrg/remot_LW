package model;

public class User {
	private String name;
    private double outstandingFine;

    public User(String name) {
        this.name = name;
        this.outstandingFine = 0.0;
    }

    public String getName() {
        return name;
    }

    public double getOutstandingFine() {
        return outstandingFine;
    }

    public void setOutstandingFine(double outstandingFine) {
        this.outstandingFine = outstandingFine;
    }

    public boolean canBorrow() {
        return outstandingFine == 0.0;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', fine=" + outstandingFine + "}";
    }

}
