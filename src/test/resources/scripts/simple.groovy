import org.eclipse.jetty.server.session.HashedSession

def hashed = request.session as HashedSession

if (!hashed.getAttribute("counter")) {
    hashed.setAttribute("counter",  1)
} else {
    hashed.setAttribute("counter", hashed.getAttribute("counter") + 1)
}

println """
<html>
    <head>
        <title>Groovy Servlet</title>
    </head>
    <body>
        <p>
Hello, ${request.remoteHost}<br /><br />You have visited site ${hashed.getAttribute("counter")} times!<br /><br /> ${new Date()}
        </p>
    </body>
</html>
"""