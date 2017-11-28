package testGetMethod;

import entity.Cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Author : Da Li
 * Created data : 十二月 07, 2016
 * Package Name: testGetMethod
 */
public class TestGet {
	public static void mainx(String[] args) {
		List<Cluster> list = new ArrayList<Cluster>();
		Cluster c1 = new Cluster("c1");
		c1.getChildren().add(new Cluster("Children"));
		list.add(c1);
		Cluster c2 = list.get(0);
		if (list.get(0).getChildren().size() == 1) {
			System.err.println("could work");
		}
		c2.getChildren().remove(0);
		if (list.get(0).getChildren().size() == 0) {
			System.err.println("could work");
		}
	}
}
