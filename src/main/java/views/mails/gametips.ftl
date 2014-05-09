<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
</head>
<body>
	<p>&{'hello'} ${user.username}!</p>
	<p>&{'mail.gametips'}</p>
	<table>
		<#list games as game>
			<tr>
				<td colspan="2"><strong>&{game.homeTeam} - &{game.awayTeam}</strong></td>
			</tr>
			<#list game.gameTips as gameTip>
				<tr>
					<td>${gameTip.user.username}</td>
					<td>${gameTip.homeScore.name} : ${gameTip.awayScore.name}</td>
				</tr>			
			</#list>
		</#list>
		<tr><td>&nbsp;</td></tr>
	</table>
	<p>
	--- <br>
	<#include footer.ftl>
	</p>
<body>
</html>