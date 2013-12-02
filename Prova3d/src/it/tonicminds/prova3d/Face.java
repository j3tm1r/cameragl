package it.tonicminds.prova3d;

public class Face {
	private Point3D p1, p2, p3;

	private Point3D n1, n2, n3;

	public Face(Point3D p1, Point3D p2, Point3D p3, Point3D n1, Point3D n2,
			Point3D n3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.n1 = n1;
		this.n2 = n2;
		this.n3 = n3;
	}

	public Face(Face face) {
		this.n1 = face.n1;
		this.n2 = face.n2;
		this.n3 = face.n3;
		this.p1 = face.p1;
		this.p2 = face.p2;
		this.p3 = face.p3;
	}

	public Point3D getN1() {
		return n1;
	}

	public void setN1(Point3D n1) {
		this.n1 = n1;
	}

	public Point3D getN2() {
		return n2;
	}

	public void setN2(Point3D n2) {
		this.n2 = n2;
	}

	public Point3D getN3() {
		return n3;
	}

	public void setN3(Point3D n3) {
		this.n3 = n3;
	}

	public Point3D getP1() {
		return p1;
	}

	public void setP1(Point3D p1) {
		this.p1 = p1;
	}

	public Point3D getP2() {
		return p2;
	}

	public void setP2(Point3D p2) {
		this.p2 = p2;
	}

	public Point3D getP3() {
		return p3;
	}

	public void setP3(Point3D p3) {
		this.p3 = p3;
	}
}
