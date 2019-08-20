<html>
<%@ page language="java" import="java.util.*" contentType="text/html; charset=utf-8" %>
<head>

</head>
<body>
<h2>Hello World!</h2>

<h1>SpingMVC上传文件</h1>

<form name="test_uploadFile_" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file">
    <input type="submit" value="SpringMVC上传文件">

    <h1>富文本图片上传文件</h1>

    <form name="test_uploadFile_" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
        <input type="file" name="upload_file">
        <input type="submit" value="富文本图片上传文件<">
</form>
</body>
</html>
