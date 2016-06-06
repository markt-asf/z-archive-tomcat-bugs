<%@ page import="java.io.*" pageEncoding="UTF-8" %>
<p>Reading input...</p>
<p><%
out.flush();
InputStream is = request.getInputStream();
Reader r = new InputStreamReader(is, "UTF-8");
BufferedReader br = new BufferedReader(r);
int read = 0;
char[] buf = new char[1024];
while (read > -1) {
	read = br.read(buf);
	if (read > 0) {
		out.write(buf, 0, read);
		out.flush();
	}
}
%></p>
<p>Reading input complete</p>
