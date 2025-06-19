<!-- src/components/AppDialog.vue -->
<template>
  <v-dialog v-model="dialog.show" max-width="500px">
    <v-card>
      <v-card-title>
        <v-icon :color="iconColor">{{ icon }}</v-icon>
        {{ dialog.title }}
      </v-card-title>
      <v-card-subtitle>{{ dialog.message }}</v-card-subtitle>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="closeDialog">OK</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useDialog } from './composables/useDialog';

const { dialog, closeDialog } = useDialog();

const icon = computed(() => {
  switch (dialog.value.type) {
    case 'error':
      return 'mdi-alert-circle';
    case 'warning':
      return 'mdi-alert';
    case 'info':
    default:
      return 'mdi-information';
  }
});

const iconColor = computed(() => {
  switch (dialog.value.type) {
    case 'error':
      return 'red';
    case 'warning':
      return 'orange';
    case 'info':
    default:
      return 'blue';
  }
});
</script>

<style scoped>
.v-dialog {
  z-index: 2000;
}
</style>
