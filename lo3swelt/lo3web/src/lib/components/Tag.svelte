<script lang="ts">
	const { label, withCross = false }: { label: string; withCross?: boolean } = $props();
</script>

<li class="wrap">
	<span class="text">
		{label + (withCross ? ' X' : '')}
	</span>
</li>

<style>
	.wrap {
		display: inline;
		&:not(:last-child) {
			&::after {
				content: ',   ';
			}
		}
	}
	.text {
		border-bottom: 1px dotted grey;
		cursor: pointer;
	}
</style>
