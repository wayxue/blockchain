package com.yitaqi.blockchain.hash;

public class HashCodeClass {

	private int int0;
	private String str0;
	private double dou0;
	public int getInt0() {
		return int0;
	}
	public void setInt0(int int0) {
		this.int0 = int0;
	}
	public String getStr0() {
		return str0;
	}
	public void setStr0(String str0) {
		this.str0 = str0;
	}
	public double getDou0() {
		return dou0;
	}
	public void setDou0(double dou0) {
		this.dou0 = dou0;
	}
	
	// 首先通过hashCode 确定对象存储的位置，如果位置相同使用equals比较对象是否相同
	// 尽量保证hashCode 是唯一生成的，可以减少equals方法执行次数
	// 如果两个类的hashCode 不同，两个类必定不相同。
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) {
			return true;
		}
		if (obj instanceof HashCodeClass) {
			HashCodeClass hcc = (HashCodeClass) obj;
			if (int0 == hcc.int0 && dou0 == hcc.dou0) {
				if (str0 != null) {
					if (str0.equals(hcc.str0)) return true;
				} else {
					if (hcc.str0 == null) return true;
				}
			}
		}
		return false;
	}
}
