package it.tonicminds.prova3d;

import java.util.ArrayList;
import java.util.List;

public class Object3D {
	private ArrayList<Face> mFaces;
	private String mName;
	private String material;

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public Object3D(String name) {
		this.mName = name;
	}

	public Object3D(String name, List<Face> faces) {
		this.mName = name;
		copyFaces(faces);
	}

	private void copyFaces(List<Face> faces) {
		for (Face face : faces) {
			addFace(face);
		}
	}

	public Object3D(Object3D obj3d) {
		copyFaces(obj3d.mFaces);
		this.mName = new String(obj3d.mName);
		if (obj3d.material != null)
			this.material = new String(obj3d.material);
	}

	public ArrayList<Face> getmFaces() {
		return mFaces;
	}

	public void addFace(Face face) {
		if (mFaces == null) {
			mFaces = new ArrayList<Face>();
		}
		mFaces.add(new Face(face));
	}
}
