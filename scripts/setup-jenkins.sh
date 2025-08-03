#!/bin/bash

set -e

echo "🚀 Setting up Jenkins for MediHelp360 CI/CD..."

# Start Jenkins and Registry
echo "📦 Starting Jenkins and Docker Registry..."
docker-compose -f docker-compose.jenkins.yml up -d jenkins registry registry-ui

# Wait for Jenkins to be ready
echo "⏳ Waiting for Jenkins to be ready..."
timeout 300 bash -c 'until curl -f http://localhost:8090/login 2>/dev/null; do sleep 10; done'

# Get initial admin password
JENKINS_PASSWORD=$(docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "PASSWORD_NOT_FOUND")

echo ""
echo "🎉 Jenkins is ready!"
echo "=================================================="
echo "📍 Jenkins URL: http://localhost:8090"
echo "🔑 Initial Admin Password: $JENKINS_PASSWORD"
echo "📦 Docker Registry: http://localhost:5001"
echo "🎨 Registry UI: http://localhost:8091"
echo "=================================================="

echo ""
echo "📋 Required Manual Steps:"
echo "1. Open http://localhost:8090 in your browser"
echo "2. Use the password above to unlock Jenkins"
echo "3. Install suggested plugins + these additional ones:"
echo "   - Docker Pipeline"
echo "   - Pipeline: Stage View"
echo "   - Blue Ocean"
echo "   - GitHub Integration"
echo "   - Slack Notification (optional)"
echo "   - SonarQube Scanner (optional)"
echo ""

# Create Jenkins job configuration
echo "📝 Creating Jenkins job configuration..."
mkdir -p jenkins-config

cat > jenkins-config/medihelp360-pipeline.xml << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <actions>
    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction plugin="pipeline-model-definition@1.8.5"/>
    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction plugin="pipeline-model-definition@1.8.5">
      <jobProperties/>
      <triggers/>
      <parameters/>
      <options/>
    </org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction>
  </actions>
  <description>MediHelp360 Microservices CI/CD Pipeline</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <hudson.plugins.jira.JiraProjectProperty plugin="jira@3.1.1"/>
    <jenkins.model.BuildDiscarderProperty>
      <strategy class="hudson.tasks.LogRotator">
        <daysToKeep>30</daysToKeep>
        <numToKeep>10</numToKeep>
        <artifactDaysToKeep>-1</artifactDaysToKeep>
        <artifactNumToKeep>-1</artifactNumToKeep>
      </strategy>
    </jenkins.model.BuildDiscarderProperty>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <com.cloudbees.jenkins.GitHubPushTrigger plugin="github@1.34.3">
          <spec></spec>
        </com.cloudbees.jenkins.GitHubPushTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.92">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.8.3">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/your-username/medihelp360.git</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
        <hudson.plugins.git.BranchSpec>
          <name>*/develop</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF

echo "✅ Jenkins configuration created at jenkins-config/medihelp360-pipeline.xml"

# Test Docker Registry
echo "🧪 Testing Docker Registry..."
if curl -f http://localhost:5001/v2/ 2>/dev/null; then
    echo "✅ Docker Registry is working"
else
    echo "❌ Docker Registry is not responding"
fi

# Show next steps
echo ""
echo "🎯 Next Steps:"
echo "1. Configure your Git repository URL in Jenkins"
echo "2. Set up webhook in your Git repository:"
echo "   Webhook URL: http://your-server:8090/github-webhook/"
echo "3. Add Docker Registry as insecure registry in Docker daemon:"
echo "   Add 'localhost:5001' to insecure-registries in Docker settings"
echo "4. Test the pipeline with a commit"
echo ""

echo "🔧 To configure Docker for insecure registry, add this to /etc/docker/daemon.json:"
echo '{'
echo '  "insecure-registries": ["localhost:5001"]'
echo '}'
echo ""

echo "🚀 Jenkins setup completed!" 