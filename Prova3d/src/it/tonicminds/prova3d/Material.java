package it.tonicminds.prova3d;

public class Material {
	/*
	 * Illumnation models
	 * 0. Color on and Ambient off
	 * 1. Color on and Ambient on
	 * 2. Highlight on
	 * 3. Reflection on and Ray trace on
	 * 4. Transparency: Glass on, Reflection: Ray trace on
     * 5. Reflection: Fresnel on and Ray trace on
     * 6. Transparency: Refraction on, Reflection: Fresnel off and Ray trace on
     * 7. Transparency: Refraction on, Reflection: Fresnel on and Ray trace on
     * 8. Reflection on and Ray trace off
     * 9. Transparency: Glass on, Reflection: Ray trace off
     * 10. Casts shadows onto invisible surfaces
	 * */
	private float mNs, mNi, mD, mI;
	private Point3D mKa, mKd, mKs;
	private int mIllum;
	private String mName;
	
	
	
	public Material(String name) {
		this.mName = name;
	}
	
	public String getmName() {
		return mName;
	}

	
	public float getmNs() {
		return mNs;
	}
	public void setmNs(float mNs) {
		this.mNs = mNs;
	}
	public float getmNi() {
		return mNi;
	}
	public void setmNi(float mNi) {
		this.mNi = mNi;
	}
	public float getmD() {
		return mD;
	}
	public void setmD(float mD) {
		this.mD = mD;
	}
	public float getmI() {
		return mI;
	}
	public void setmI(float mI) {
		this.mI = mI;
	}
	public Point3D getmKa() {
		return mKa;
	}
	public void setKa(Point3D mKa) {
		this.mKa = mKa;
	}
	public Point3D getmKd() {
		return mKd;
	}
	public void setmKd(Point3D mKd) {
		this.mKd = mKd;
	}
	public Point3D getmKs() {
		return mKs;
	}
	public void setmKs(Point3D mKs) {
		this.mKs = mKs;
	}
	public int getmIllum() {
		return mIllum;
	}
	public void setmIllum(int mIllum) {
		this.mIllum = mIllum;
	}	
}
