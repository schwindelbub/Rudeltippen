<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
</head>
<body>
	<p>&{'hello'} ${user.username}!</p>
	<p>&{'mail.placetips'}</p>
	<table>
		<tr>
			<td><strong>&{'mail.games'}</strong></td>
		</tr>
		<#if games??>
			<#list games as game>
				<tr>
					<td>${game.homeTeam.name} - ${game.awayTeam.name}</td>
				</tr>
				<tr>
					<td>&{'mail.tipto'}: ${game.tippEnding.formatted()}</td>
				</tr>
				<#if game_has_next>
					<tr>
						<td>&nbsp;</td>
					</tr>			
				</#if>
			</#list>
		<#else>
			<tr>
				<td>&{'mail.nogames'}</td>
			</tr>
		</#if>
	</table>
	<br /><br />
	<table>
		<tr>
			<td><strong>&{'extratips'}</strong></td>
		</tr>
		<#if extras??>
		<#list extras as extra>
			<tr>
				<td>${extra.question}</td>
			</tr>
			<tr>
				<td>&{'mail.tipto'}: ${extra.ending?datetime}</td>
			</tr>
			<#if extra_has_next>
				<tr>
					<td>&nbsp;</td>
				</tr>			
			</#if>
		</#list>
		<#else>
			<tr>
				<td>&{'mail.noextras'}</td>
			</tr>
		</#if>
	</table>
	<p>
	--- <br>
	<#include footer.ftl>
	</p>
<body>
</html>