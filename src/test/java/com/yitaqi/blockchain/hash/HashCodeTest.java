package com.yitaqi.blockchain.hash;

import org.junit.Assert;
import org.junit.Test;

public class HashCodeTest {

	@Test
	public void test() {
		System.out.println(new HashCodeClass().hashCode() % 16);
		System.out.println(new HashCodeClass().hashCode() % 16);
		System.out.println(new HashCodeClass().hashCode() % 16);
		System.out.println(new HashCodeClass().hashCode() % 16);
		System.out.println(new HashCodeClass().hashCode() % 16);
		Assert.assertTrue(new HashCodeClass().equals(new HashCodeClass()));
	}
}
