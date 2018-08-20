package framework;

import base.Constant.DedupEngine;
import base.Constant.SamplingMethod;

public class User {

	private int userId;
	
	private int R;
	
	private DedupEngine engine;
	
	private SamplingMethod method;

	public User(int userId, int R, DedupEngine engine, SamplingMethod method) {
		super();
		this.userId = userId;
		this.R = R;
		this.engine = engine;
		this.method = method;
	}

	public int getUserId() {
		return userId;
	}

	public int getR() {
		return R;
	}

	public DedupEngine getEngine() {
		return engine;
	}

	public SamplingMethod getMethod() {
		return method;
	}
}
