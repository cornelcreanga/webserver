<!DOCTYPE html>
<html>
<head>
    <title>${statusCode}-${statusReason}</title>
</head>
<body>
    <h1>${statusReason}</h1>
    <#if extendedReason?has_content>
        <p>${extendedReason}</p>
    </#if>
</body>
</html>