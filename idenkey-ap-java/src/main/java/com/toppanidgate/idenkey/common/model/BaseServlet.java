package com.toppanidgate.idenkey.common.model;

import javax.servlet.http.HttpServlet;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public abstract class BaseServlet extends HttpServlet {
	protected static final Gson gson = new Gson();
}
