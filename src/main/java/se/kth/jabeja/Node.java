package se.kth.jabeja;

import java.util.ArrayList;

public class Node {

	private double temp;
	private double cost;
	private int id;
	private int color;
	private int initColor;
	private ArrayList<Integer> neighbours;

	public Node(int id, int color) {
		this.id = id;
		this.temp = 2;
		this.color = color;
		this.initColor = color;
		this.neighbours = new ArrayList<Integer>();
		this.cost = 0;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setNeighbours(ArrayList<Integer> neighbours) {
		for (int id : neighbours)
			this.neighbours.add(id);
	}


	public double getCost() {
		return this.cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getTemp() { return temp; }
	public void setTemp(double temp) { this.temp = temp; }
	public int getId() {
		return this.id;
	}
	public int getColor() {
		return this.color;
	}
	public int getDegree() {
		return this.neighbours.size();
	}
	public int getInitColor() {
		return this.initColor;
	}
	public ArrayList<Integer> getNeighbours() {
		return this.neighbours;
	}
	@Override
	public String toString() {
		return "id: " + id + ", color: " + color + ", neighbours: " + neighbours + "\n";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Node node = (Node) o;

		if (Double.compare(node.temp, temp) != 0) return false;
		if (Double.compare(node.cost, cost) != 0) return false;
		if (id != node.id) return false;
		if (color != node.color) return false;
		if (initColor != node.initColor) return false;
		return neighbours != null ? neighbours.equals(node.neighbours) : node.neighbours == null;
	}

	@Override
	public int hashCode() {
		int result;
		long temp1;
		temp1 = Double.doubleToLongBits(temp);
		result = (int) (temp1 ^ (temp1 >>> 32));
		temp1 = Double.doubleToLongBits(cost);
		result = 31 * result + (int) (temp1 ^ (temp1 >>> 32));
		result = 31 * result + id;
		result = 31 * result + color;
		result = 31 * result + initColor;
		result = 31 * result + (neighbours != null ? neighbours.hashCode() : 0);
		return result;
	}
}
