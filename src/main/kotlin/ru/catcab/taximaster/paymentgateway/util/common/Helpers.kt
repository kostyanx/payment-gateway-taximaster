package ru.catcab.taximaster.paymentgateway.util.common

import org.apache.logging.log4j.core.util.StringBuilderWriter
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.regex.Pattern
import javax.xml.bind.DatatypeConverter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult




object Helpers {
    @JvmStatic val LEADING_ZEROES_REGEXP = "^0+(?!\$)".toRegex()
    @JvmStatic val VAR_PATTERN = Pattern.compile("\\{([A-Za-z_][A-Za-z0-9._]*)}")!!

    val nowExpression = object : Expression<LocalDateTime>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { +"NOW()" }
    }

    fun String.removeLeadingZeros(): String {
        return replaceFirst(LEADING_ZEROES_REGEXP, "")
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun String.containsAddress(remoteHost: String): Boolean {
        val remoteAddress = Inet4Address.getByName(remoteHost)
        val (subnetAddress, subnetMaskCidr) = if (contains('/')) {
            val arr = split('/')
            Inet4Address.getByName(arr[0]) to arr[1].toInt()
        } else {
            Inet4Address.getByName(this) to 32
        }
        val subnetMaskInt = (0xFFFFFFFF ushr subnetMaskCidr).inv().toInt()
        val subnetAddressInt = ByteBuffer.wrap(subnetAddress.address).int
        val remoteAddressInt = ByteBuffer.wrap(remoteAddress.address).int
        return subnetAddressInt and subnetMaskInt == remoteAddressInt and subnetMaskInt
    }

    fun resolve(template: String?, recursive: Boolean, resolver: (String) -> String?): String {
        if (template == null) return ""
        val matcher = VAR_PATTERN.matcher(template)
        val sb = StringBuilder()
        while (matcher.find()) {
            val match = template.substring(matcher.start(1), matcher.end(1))
            val value = resolver(match)
            val resultValue = if (recursive) resolve(value, true, resolver) else value
            matcher.appendReplacement(sb, resultValue ?: "")
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    fun ccbSignatureIsValid(sourceXml: String, contentTag: String, signTag: String, salt: String): Boolean {
        val factory = DocumentBuilderFactory.newInstance()!!
        val builder = factory.newDocumentBuilder()!!
        val document = builder.parse(ByteArrayInputStream(sourceXml.encodeToByteArray()))!!
        val root = requireNotNull(document.documentElement)
        val content = requireNotNull(root.getElementsByTagName(contentTag).item(0))
        val sign = requireNotNull(root.getElementsByTagName(signTag).item(0))
        val signMd5 = requireNotNull(sign.textContent).trim().toUpperCase()

        val transformerFactory = TransformerFactory.newInstance()!!
        val transformer = transformerFactory.newTransformer()!!
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        val domSource = DOMSource(content)
        val stringBuilderWriter = StringBuilderWriter()
        val streamResult = StreamResult(stringBuilderWriter)
        transformer.transform(domSource, streamResult)
        val contentWithTag = stringBuilderWriter.builder.toString()

        val tagLength = contentTag.length + 2
        val contentStr = contentWithTag.substring(tagLength, contentWithTag.length - tagLength - 1)
        val contentWithSalt = contentStr + salt

        val md = MessageDigest.getInstance("MD5")
        md.update(contentWithSalt.encodeToByteArray())
        val calculatedMd5 = DatatypeConverter.printHexBinary(md.digest()).toUpperCase()

        return calculatedMd5 == signMd5
    }
}