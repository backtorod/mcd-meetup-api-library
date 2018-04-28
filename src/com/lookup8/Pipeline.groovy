#!/usr/bin/groovy
package com.lookup8;

/*
    Set the kubernetes namespace based on git branch
*/
def setNamespace(branchName) {
    
    def String namespace
    
    if (branchName == "master") {
        namespace = "production"
    } else if (branchName == "develop" ) {
        namespace = "develop"
    } else {
        namespace = branchName.toLowerCase()
    }

    return namespace

}

/*
    Delete namespace
*/
def deleteNamespace(String namespace) {
    container('kubectl') {
        println "Deleting namespace ${namespace}"
        sh "kubectl delete namespace ${namespace}"
    }
}

/*
    Sanity Checks for Required Tools
*/
def sanityCheck(containerName, checkCommand) {
    container(containerName) {
        sh "${checkCommand}"
    }
}

/*
    Cleanup
*/
def removeTestContainer(containerName, testContainerName) {
    container(containerName) {
        sh "docker ps --filter 'name=${testContainerName}' || true && docker rm -f ${testContainerName} || true"
    }
}

/*
    Helm
*/
def helmLint(String chartDir) {
    // lint helm chart
    println "running helm lint ${chartDir}"
    sh "helm lint ${chartDir}"

}

def helmDeploy(Map args) {
    if (args.dryRun) {
        println "Running dry-run deployment"
        sh "helm upgrade ${args.name} ${args.chartDir} --dry-run --install --set component=${args.name},image=${args.set.imageName},imageTag=${args.set.commitHash},replicas=${args.set.replicas},cpu=${args.set.cpu} --namespace=${args.namespace}"
    } else {
        println "Running deployment"
        sh "helm upgrade ${args.name} ${args.chartDir} --install --set component=${args.name},image=${args.set.imageName},imageTag=${args.set.commitHash},replicas=${args.set.replicas},cpu=${args.set.cpu},serviceType=${args.set.serviceType},servicePort=${args.set.servicePort},containerPort=${args.set.containerPort},environment.databaseHost=${args.set.databaseHost},environment.databasePort=${args.set.databasePort},environment.databaseUser=${args.set.databaseUser},environment.databasePass=${args.set.databasePass},environment.databaseName=${args.set.databaseName} --namespace=${args.namespace}"
    }
}

def helmDelete(String releaseName) {
    println "Running helm delete ${releaseName}"
    sh "helm delete --purge ${releaseName}"
}