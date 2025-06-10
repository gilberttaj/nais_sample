<template>
  <PageLayout>
    <!-- Hero Section -->
    <section class="bg-gradient-to-r from-primary to-primary-dark text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
        <div class="flex flex-col lg:flex-row items-center">
          <div class="lg:w-1/2 mb-12 lg:mb-0 text-center lg:text-left">
            <h1 class="text-4xl md:text-5xl font-bold mb-6">Welcome to NAIS Platform</h1>
            <p class="text-lg mb-8 opacity-90">The ultimate solution for your digital needs. Discover a seamless experience designed for modern users.</p>
            <div class="flex flex-wrap justify-center lg:justify-start gap-4">
              <router-link to="/signup" class="px-8 py-3 rounded-md text-primary bg-white font-semibold text-base shadow-lg hover:bg-gray-100 transition-all">
                Get Started
              </router-link>
              <a href="#features" class="px-8 py-3 rounded-md bg-transparent border-2 border-white font-semibold text-base hover:bg-white hover:text-primary transition-all">
                Learn More
              </a>
            </div>
          </div>
          <div class="lg:w-1/2 flex justify-center lg:justify-end">
            <div class="relative">
              <div class="absolute -top-6 -right-6 w-64 h-64 bg-white opacity-10 rounded-full"></div>
              <div class="relative bg-white p-6 rounded-lg shadow-xl">
                <div class="w-72 h-64 bg-gray-200 rounded-lg flex items-center justify-center">
                  <div class="text-6xl text-primary flex items-center">
                    <span class="mr-2">NAIS</span>
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="py-20 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-extrabold text-gray-900 sm:text-4xl">
            Amazing Features
          </h2>
          <p class="mt-4 max-w-2xl text-xl text-gray-500 mx-auto">
            Discover the powerful capabilities that set our platform apart.
          </p>
        </div>

        <div class="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-3">
          <!-- Feature 1 -->
          <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-all">
            <div class="w-12 h-12 bg-primary-dark rounded-md flex items-center justify-center text-white mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">Secure Authentication</h3>
            <p class="text-gray-600">
              State-of-the-art security features to keep your data safe and protected at all times.
            </p>
          </div>

          <!-- Feature 2 -->
          <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-all">
            <div class="w-12 h-12 bg-primary-dark rounded-md flex items-center justify-center text-white mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">Lightning Fast</h3>
            <p class="text-gray-600">
              Optimized performance ensures quick loading times and smooth user experience.
            </p>
          </div>

          <!-- Feature 3 -->
          <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-all">
            <div class="w-12 h-12 bg-primary-dark rounded-md flex items-center justify-center text-white mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">Advanced Analytics</h3>
            <p class="text-gray-600">
              Comprehensive data insights to help you make informed decisions and grow.
            </p>
          </div>

          <!-- Feature 4 -->
          <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-all">
            <div class="w-12 h-12 bg-primary-dark rounded-md flex items-center justify-center text-white mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">24/7 Support</h3>
            <p class="text-gray-600">
              Our dedicated team is always available to assist you with any questions.
            </p>
          </div>

          <!-- Feature 5 -->
          <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-all">
            <div class="w-12 h-12 bg-primary-dark rounded-md flex items-center justify-center text-white mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
              </svg>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">Customizable Interface</h3>
            <p class="text-gray-600">
              Tailor the platform to your specific needs and preferences with ease.
            </p>
          </div>

          <!-- Feature 6 -->
          <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-all">
            <div class="w-12 h-12 bg-primary-dark rounded-md flex items-center justify-center text-white mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z" />
              </svg>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">Integrated Communication</h3>
            <p class="text-gray-600">
              Seamless tools for messaging and collaboration with your team and clients.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="bg-gray-900 text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div class="lg:flex lg:items-center lg:justify-between">
          <div>
            <h2 class="text-3xl font-extrabold tracking-tight sm:text-4xl">
              Ready to get started?
            </h2>
            <p class="mt-3 max-w-3xl text-lg">
              Join thousands of satisfied users who have transformed their digital experience with our platform.
            </p>
          </div>
          <div class="mt-8 flex lg:mt-0 lg:flex-shrink-0">
            <div class="inline-flex rounded-md shadow">
              <router-link to="/signup" class="px-5 py-3 bg-primary rounded-md font-medium text-white hover:bg-primary-dark transition-all">
                Sign Up Now
              </router-link>
            </div>
            <div class="ml-3 inline-flex rounded-md shadow">
              <router-link to="/signin" class="px-5 py-3 bg-white text-gray-900 rounded-md font-medium hover:bg-gray-100 transition-all">
                Sign In
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>
  </PageLayout>
</template>

<script>
import PageLayout from '../components/templates/PageLayout.vue'

export default {
  name: 'HomePage',
  components: {
    PageLayout
  }
}
</script>
