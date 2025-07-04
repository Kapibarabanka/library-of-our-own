<script lang="ts">
    import { browser } from '$app/environment';
    import { PUBLIC_BOTNAME } from '$env/static/public';
</script>

{#if browser}
    <script
        async
        src="https://telegram.org/js/telegram-widget.js?22"
        data-telegram-login={PUBLIC_BOTNAME}
        data-size="medium"
        data-auth-url="/login"
        data-request-access="write"
    ></script>
{/if}

<style></style>
