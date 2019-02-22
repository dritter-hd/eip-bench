package tpc.h;

import pdgf.util.random.PdgfDefaultRandom;

public class LineItemRandTest {
	public static void main(String[] args) {
		PdgfDefaultRandom r = new PdgfDefaultRandom();
		int scale = 10;
		int lineitemSize = 6000000; // 6mio
		int ordersSize = 1500000;// 1.5 mio
		int orderRows = ordersSize * scale;

		int ordercount[] = new int[orderRows];
		long rand;
		int max = 0;
		int curRow = 0;
		for (int i = 0; i < lineitemSize * scale; i++) {
			rand = r.nextLong();
			if (rand < 0) {
				rand = -rand;
			}

			rand = rand % orderRows;
			curRow = (int) rand;
			ordercount[curRow]++;
			max = Math.max(max, ordercount[curRow]);
		}

		System.out.println("max: " + max);

		// search min && calc mean
		int min = Integer.MAX_VALUE;
		long count = 0;
		for (int i = 0; i < ordercount.length; i++) {
			min = Math.min(min, ordercount[i]);
			count += ordercount[i];
		}
		System.out.println("min: " + min);
		System.out.println("count: " + count);
		System.out.println("mean: " + count / ordercount.length);

		for (int i = ordercount.length - 100; i < ordercount.length; i++) {
			System.out.println(ordercount[i]);
		}
	}
}
