# L34T

L34T is an XML-based template language that allows you to create XML
and XHTML documents from an XML template.  For variable substitutions,
you use XPath syntax inside attributes and the XSLT `value-of`
function with XPath syntax inside element content.  You can also use
XSLT iteration such as `for-each` and `choose`/`when`.

Inputs to the template can be a set of variables, values, and types
specified on the command line, and can also be include an XML input
file.  The template can use the `l34t:instance()` function to access
the input xml document, and can use the `document` function to read
files.

The name L34T stands for "Literal-Result-Element-As-Stylesheet", or
"L" followed by 34 letters, followed by "T".  The feature of having a
reduced set of XSLT just being the result document itself has existed
in XSLT since hte beginning, but it was always thought that there was
no way to pass in variables, making it almost useless.  L34T starts
with the realization that you can programmatically create a stylesheet
containing the variables which then includes the template, and it
works flawlessly.

# Input

# L34T Template
Template with variables

    <?xml version="1.0" encoding="UTF-8"?>
    <fred xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xsl:version="2.0" >
      <bar><xsl:value-of select="$pizza" /></bar>
    </fred>

or, a more complex temlate that also uses access to an XML instance input document

    <?xml version="1.0" encoding="UTF-8"?>
    <pizza xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xsl:version="2.0"
           xmlns:l34t='http://software.hipme.com/2014/saxon-extension' exclude-result-prefixes='l34t'>

      <bar><xsl:value-of select="$pizza" /></bar>
      <foo a="{$x * $x}">x=<xsl:value-of select="$x" /></foo>
      <instancedoc><xsl:copy-of select="l34t:instance()" /></instancedoc>
    </pizza>

# Compile 
    export CLASSPATH=$HOME/java/SaxonHE9/saxon9he.jar:classes

    rm -rf classes && mkdir classes
    javac -d classes -Xlint:deprecation L34T.java || exit


# Run

With and the more complex template above and this input document:

    <?xml version="1.0" encoding="UTF-8"?>
    <document>
      This is the input document.
    </document>

`java com.hipme.software.l34t.L34T L34T.xml instance.xml x int 3 pizza string fred`

    <?xml version="1.0" encoding="UTF-8"?>
    <pizza>fred</pizza>
    <foo a="9">x=3</foo>
    <instancedoc>
       <document>
      This is the input document.
    </document>
    </instancedoc>

# Notes
If you want to add your own extension functions your copy of L34T, Look at https://github.com/twl8n/saxon-9-he-samples for more info on writing extension functions.

----
(C) 2014 Leigh L. Klotz, Jr.
http://software.hipme.com/L34T
