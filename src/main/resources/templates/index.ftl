<!DOCTYPE html>
<html>
<head>
    <title>Index of ${folder}</title>
</head>
<body>
<h1>Index of ${folder}</h1>
<table>
    <tr>
        <th>Name</th>
        <th>Last modified</th>
        <th>Size</th>
    </tr>
    <tr>
        <th colspan="3">
            <hr>
        </th>
    </tr>

<#if allowBrowsing?has_content>
    <tr>
        <td>
            <a href="${parentFolder}">Go to Parent Directory </a>
        </td>
        <td align="right">&nbsp;</td>
        <td align="right">&nbsp;</td>
<tr>
</#if>


<#list folderFiles as file>
    <tr>
        <td>
            <a href="${file.link}">${file.name}</a>
        </td>
        <td align="right">${file.lastModified}</td>
        <td align="right">${file.size}</td>
<tr>
</#list>
    <tr>
        <th colspan="3">
            <hr>
        </th>
    </tr>
</table>
</body>
</html>