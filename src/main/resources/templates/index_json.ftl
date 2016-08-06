{
    "files": [
        <#list folderFiles as file>
        {
            "name": "${file.name}",
            "lastModified": "${file.lastModified}",
            "size": "${file.size}",
            "type": "${file.type}",
            "link": "${file.link}"
        }
        </#list>
    ]
}