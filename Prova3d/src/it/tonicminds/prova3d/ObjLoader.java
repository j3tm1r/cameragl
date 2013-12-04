package it.tonicminds.prova3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.content.Context;
import android.support.v4.util.ArrayMap;

public class ObjLoader {
	private Context mContext;
	private ArrayList<Object3D> mObjects3d;
	private ArrayMap<String, Material> mMaterials;

	public ArrayList<Object3D> getObjects3d() {
		return mObjects3d;
	}
	
	public ArrayMap<String,Material> getMaterials() {
		return mMaterials;
	}

	public ObjLoader(String fileName, Context context) {
		this.mContext = context;
		mObjects3d = new ArrayList<Object3D>();
		mMaterials = new ArrayMap<String, Material>();
		loadModel(fileName + ".obj");
		loadMaterials(fileName + ".mtl");
	}

	private void loadMaterials(String filename) {
		String mLine;
		Material objTmp = null;
		Point3D p;
		float x, y, z, d, ni, ns;
		int illum;
		String[] substrings;
		String material_name = "" ;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					mContext.getAssets().open(filename)));

			// do reading, usually loop until end of file reading

			while ((mLine = reader.readLine()) != null) {
				substrings = mLine.split("\\s+");

				if (substrings[0].equalsIgnoreCase("newmtl")) {
					objTmp = new Material(substrings[1]);
					material_name = new String(substrings[1]);
					mMaterials.put(objTmp.getmName(), objTmp);
					
				} else if (substrings[0].equalsIgnoreCase("Ka")) {
					x = Float.parseFloat(substrings[1]);
					y = Float.parseFloat(substrings[2]);
					z = Float.parseFloat(substrings[3]);
					p = new Point3D(x, y, z);
					mMaterials.get(material_name).setKa(p);
				} else if (substrings[0].equalsIgnoreCase("Kd")) {
					x = Float.parseFloat(substrings[1]);
					y = Float.parseFloat(substrings[2]);
					z = Float.parseFloat(substrings[3]);
					p = new Point3D(x, y, z);
					mMaterials.get(material_name).setmKd(p);
				} else if (substrings[0].equalsIgnoreCase("Ks")) {
					x = Float.parseFloat(substrings[1]);
					y = Float.parseFloat(substrings[2]);
					z = Float.parseFloat(substrings[3]);
					p = new Point3D(x, y, z);
					mMaterials.get(material_name).setmKs(p);
				} else if (substrings[0].equalsIgnoreCase("Ns")) {
					ns = Float.parseFloat(substrings[1]);
					mMaterials.get(material_name).setmNs(ns);
				} else if (substrings[0].equalsIgnoreCase("Ni")) {
					ni = Float.parseFloat(substrings[1]);
					mMaterials.get(material_name).setmNi(ni);
				} else if (substrings[0].equalsIgnoreCase("d")) {
					d = Float.parseFloat(substrings[1]);
					mMaterials.get(material_name).setmD(d);
				} else if (substrings[0].equalsIgnoreCase("illum")) {
					illum = Integer.parseInt(substrings[1]);
					mMaterials.get(material_name).setmIllum(illum);
				}
			}

			reader.close();
		} catch (IOException e) {
			System.out.println("Error reading obj file");
			throw new RuntimeException(e.getMessage());
		}
	}

	private void loadModel(String filename) {
		String mLine;
		Object3D objTmp = null;
		ArrayList<Point3D> vertices = new ArrayList<Point3D>();
		ArrayList<Point3D> normals = new ArrayList<Point3D>();
		float x, y, z;
		String[] substrings;
		int p1, p2, p3, n1, n2, n3;
		int index = -1;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					mContext.getAssets().open(filename)));

			// do reading, usually loop until end of file reading

			while ((mLine = reader.readLine()) != null) {
				substrings = mLine.split("\\s+");

				if (substrings[0].equalsIgnoreCase("o")) {

					if (objTmp == null) {
						objTmp = new Object3D(mLine.substring(mLine
								.indexOf(" ") + 1));
					} else {
						objTmp = new Object3D(substrings[1]);
					}
					mObjects3d.add(objTmp);
					index++;
				} else if (substrings[0].equalsIgnoreCase("v")) {
					x = Float.parseFloat(substrings[1]);
					y = Float.parseFloat(substrings[2]);
					z = Float.parseFloat(substrings[3]);
					vertices.add(new Point3D(x, y, z));
				} else if (substrings[0].equalsIgnoreCase("vn")) {
					x = Float.parseFloat(substrings[1]);
					y = Float.parseFloat(substrings[2]);
					z = Float.parseFloat(substrings[3]);
					normals.add(new Point3D(x, y, z));
				} else if (substrings[0].equalsIgnoreCase("f")) {
					p1 = Integer.parseInt(substrings[1].split("//")[0]) - 1;
					n1 = Integer.parseInt(substrings[1].split("//")[1]) - 1;
					p2 = Integer.parseInt(substrings[2].split("//")[0]) - 1;
					n2 = Integer.parseInt(substrings[2].split("//")[1]) - 1;
					p3 = Integer.parseInt(substrings[3].split("//")[0]) - 1;
					n3 = Integer.parseInt(substrings[3].split("//")[1]) - 1;

					mObjects3d.get(index).addFace(
							new Face(vertices.get(p1), vertices.get(p2),
									vertices.get(p3), normals.get(n1), normals
											.get(n2), normals.get(n3)));
				} else if (substrings[0].equalsIgnoreCase("usemtl")) {
					mObjects3d.get(index).setMaterial(substrings[1]);
				}
			}

			reader.close();
		} catch (IOException e) {
			System.out.println("Error reading obj file");
			throw new RuntimeException(e.getMessage());
		}
	}
}
