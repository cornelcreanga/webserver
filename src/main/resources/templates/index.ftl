<!DOCTYPE html>
<html>
<head>
    <title>Index of ${parentFolder}</title>
</head>
<body>
<h1>Index of ${parentFolder}</h1>
<table>
    <tr>
        <th>Name</th>
        <th>Last modified</th>
        <th>Size</th>
        <th>Type</th>
    </tr>
    <tr><th colspan="4"><hr></th></tr>
    <#list folderFiles as file>
        <tr>
            <td>
                <a href="${file.link}">${file.name}</a>
            </td>
            <td align="right">${file.lastModified}</td>
            <td align="right">${file.size}</td>
            <td align="right">${file.type}</td>
        <tr>
    </#list>
    <tr><th colspan="4"><hr></th></tr>
</table>
</body>
</html>