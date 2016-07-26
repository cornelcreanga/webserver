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

    <#list folderFiles as file>
        <tr>
            <td>${file.name}</td>
            <td>${file.lastModified}</td>
            <td>${file.size}</td>
            <td>${file.type}</td>
        <tr>
    </#list>

</table>
</body>
</html>