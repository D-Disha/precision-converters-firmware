// This variable holds the build status
buildStatusInfo = "Success"

def doBuild(projectName) {
	Map buildMap =[:]
	if (env.CHANGE_TARGET == "main" || env.CHANGE_TARGET == "develop") {
		// This map is for building all targets when merging to main or develop branches
		buildMap = [
			PLATFORM_NAME: ['SDP_K1', 'NUCLEO_L552ZE_Q', 'DISCO_F769NI'],
			EVB_INTERFACE: ['ARDUINO','SDP_120'],
			EVB_TYPE: ['DEV_AD4111','DEV_AD4112', 'DEV_AD4114', 'DEV_AD4115', 'DEV_AD7172_2', 'DEV_AD7172_4', 'DEV_AD7173_8', 'DEV_AD7175_2', 'DEV_AD7175_8', 'DEV_AD7176_2', 'DEV_AD7177_2']
		]
	} else {
		// This map is for building targets that is always build
		buildMap = [
			PLATFORM_NAME: ['SDP_K1'],
			EVB_INTERFACE: ['SDP_120'],
			EVB_TYPE: ['DEV_AD4111']
		]
	}

	// Get the matrix combination and filter them to exclude unrequired matrixes
	List buildMatrix = buildInfo.getMatrixCombination(buildMap).findAll { axis ->
		// Skip 'all platforms except SDP-K1 + SDP_120 EVB interface'
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_INTERFACE'] == 'SDP_120') &&
    	!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_INTERFACE'] == 'SDP_120') &&

		// Skip all devices except AD4111 for NUCLEO_L552ZE_Q and DISCO_F769NI boards 
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD4112') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD4114') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD4115') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD7172_2') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD7172_4') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD7173_8') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD7175_2') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD7175_8') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD7176_2') &&
		!(axis['PLATFORM_NAME'] == 'NUCLEO_L552ZE_Q' && axis['EVB_TYPE'] == 'DEV_AD7177_2') &&
		
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD4112') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD4114') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD4115') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD7172_2') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD7172_4') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD7173_8') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD7175_2') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD7175_8') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD7176_2') &&
		!(axis['PLATFORM_NAME'] == 'DISCO_F769NI' && axis['EVB_TYPE'] == 'DEV_AD7177_2')
	}

	def buildType = buildInfo.getBuildType()
	if (buildType == "sequential") {
		node(label : "${buildInfo.getBuilderLabel(projectName: projectName)}") {
			ws('workspace/pcg-fw') {
				checkout scm
				try {
					for (int i = 0; i < buildMatrix.size(); i++) {
						Map axis = buildMatrix[i]
						runSeqBuild(axis, projectName)
					}
				}
				catch (Exception ex) {
					echo "Failed in ${projectName}-Build"
					echo "Caught:${ex}"
					buildStatusInfo = "Failed"
					currentBuild.result = 'FAILURE'
				}
				deleteDir()
			}
		}
	}
	else {
		// Build all included matrix combinations
		buildMap = [:]
		for (int i = 0; i < buildMatrix.size(); i++) {
			// Convert the Axis into valid values for withEnv step
			Map axis = buildMatrix[i]
			List axisEnv = axis.collect { key, val ->
				"${key}=${val}"
			}

			buildMap[axisEnv.join(', ')] = { ->
				ws('workspace/pcg-fw') {
					checkout scm
					withEnv(axisEnv) {
						try {
							stage("Build-${PLATFORM_NAME}-${EVB_INTERFACE}-${EVB_TYPE}") {

								echo "^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^"     
								echo "Running on node: '${env.NODE_NAME}'"
							
								echo "TOOLCHAIN: ${TOOLCHAIN}"
								echo "TOOLCHAIN_PATH: ${TOOLCHAIN_PATH}"
							
								echo "Building for ${PLATFORM_NAME} and ${EVB_TYPE} with ${EVB_INTERFACE} EVB"
								echo "^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^"
							
								echo "Starting mbed build..."
								//NOTE: if adding in --profile, need to change the path where the .bin is found by mbedflsh in Test stage
								bat "cd projects/${projectName} & make test TARGET_BOARD=${PLATFORM_NAME} ARTIFACT-NAME=${PLATFORM_NAME}-${EVB_INTERFACE}-${EVB_TYPE} TEST_FLAGS=-DPLATFORM_NAME=\\\\\\\"${PLATFORM_NAME}\\\\\\\" TEST_FLAGS+=-D${EVB_TYPE} TEST_FLAGS+=-D${EVB_INTERFACE}"
								artifactory.uploadFirmwareArtifacts("projects/${projectName}/build/${PLATFORM_NAME}/${TOOLCHAIN}","${projectName}")
								archiveArtifacts allowEmptyArchive: true, artifacts: "projects/${projectName}/build/**/*.bin, projects/${projectName}/build/**/*.elf"
								stash includes: "projects/${projectName}/build/**/*.bin, projects/${projectName}/build/**/*.elf", name: "${PLATFORM_NAME}-${EVB_INTERFACE}-${EVB_TYPE}"
								deleteDir()
							}
						}
						catch (Exception ex) {
								echo "Failed in Build-${PLATFORM_NAME}-${EVB_INTERFACE}-${EVB_TYPE} stage"
								echo "Caught:${ex}"
								buildStatusInfo = "Failed"
								currentBuild.result = 'FAILURE'
								deleteDir()
						}
					}
				}
			}
		}

		stage("Matrix Builds") {
			parallel(buildMap)
		}
	}

	return buildStatusInfo
}

def runSeqBuild(Map config =[:], projectName) {
	stage("Build-${config.PLATFORM_NAME}-${config.EVB_INTERFACE}-${config.EVB_TYPE}") {

		echo "^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^"     
		echo "Running on node: '${env.NODE_NAME}'"
	
		echo "TOOLCHAIN: ${TOOLCHAIN}"
		echo "TOOLCHAIN_PATH: ${TOOLCHAIN_PATH}"
	
		echo "Building for ${config.PLATFORM_NAME} and ${config.EVB_TYPE} with ${config.EVB_INTERFACE} EVB"
		echo "^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^"
	
		echo "Starting mbed build..."
		//NOTE: if adding in --profile, need to change the path where the .bin is found by mbedflsh in Test stage
		bat "cd projects/${projectName} & make test TARGET_BOARD=${config.PLATFORM_NAME} ARTIFACT-NAME=${config.PLATFORM_NAME}-${config.EVB_INTERFACE}-${config.EVB_TYPE} TEST_FLAGS=-DPLATFORM_NAME=\\\\\\\"${config.PLATFORM_NAME}\\\\\\\" TEST_FLAGS+=-D${config.EVB_TYPE} TEST_FLAGS+=-D${config.EVB_INTERFACE}"
		artifactory.uploadFirmwareArtifacts("projects/${projectName}/build/${config.PLATFORM_NAME}/${TOOLCHAIN}","${projectName}")
		archiveArtifacts allowEmptyArchive: true, artifacts: "projects/${projectName}/build/**/*.bin, projects/${projectName}/build/**/*.elf"
		stash includes: "projects/${projectName}/build/**/*.bin, projects/${projectName}/build/**/*.elf", name: "${config.PLATFORM_NAME}-${config.EVB_INTERFACE}-${config.EVB_TYPE}"
		bat "cd projects/${projectName} & make clean TARGET_BOARD=${config.PLATFORM_NAME}"
	}
}

def doTest(projectName) {
	Map testMap =[:]
	if (env.CHANGE_TARGET == "main" || env.CHANGE_TARGET == "develop") {
		// This map is for testing all targets when merging to main or develop branches
		testMap = [
			PLATFORM_NAME: ['SDP_K1'],
			EVB_INTERFACE: ['SDP_120'],
			EVB_TYPE: ['DEV_AD4111']
		]
	} else {
		// This map is for testing target that is always tested
		testMap = [
			PLATFORM_NAME: ['SDP_K1'],
			EVB_INTERFACE: ['SDP_120'],
			EVB_TYPE: ['DEV_AD4111']
		]
	}

	List testMatrix = buildInfo.getMatrixCombination(testMap)
	for (int i = 0; i < testMatrix.size(); i++) {
		Map axis = testMatrix[i]
		runTest(axis, projectName)
	}
}

def runTest(Map config =[:], projectName) {
	node(label : "${hw.getAgentLabel(platform_name: config.PLATFORM_NAME, evb_ref_id: "EVAL-" + config.EVB_TYPE + "SDZ")}") {
		ws('workspace/pcg-fw') {
			checkout scm
			try {
				stage("Test-${config.PLATFORM_NAME}-${config.EVB_INTERFACE}-${config.EVB_TYPE}") {
					// Fetch the stashed files from build stage
					unstash "${config.PLATFORM_NAME}-${config.EVB_INTERFACE}-${config.EVB_TYPE}"

					echo "^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^"                       
					echo "Running on node: '${env.NODE_NAME}'"
					echo "Testing for ${config.PLATFORM_NAME} and ${config.EVB_TYPE} with ${config.EVB_INTERFACE} EVB"
					echo "^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^ ^^**^^"
					
					platformInfo = hw.getPlatformInfo(platform_name: config.PLATFORM_NAME, evb_ref_id: "EVAL-" + config.EVB_TYPE + "SDZ")
					echo "Programming MCU platform..."               
					bat "mbedflsh --version"
					// need to ignore return code from mbedflsh as it returns 1 when programming successful
					bat returnStatus: true , script: "mbedflsh --disk ${platformInfo["mountpoint"]} --file ${WORKSPACE}\\projects\\${projectName}\\build\\${config.PLATFORM_NAME}\\${TOOLCHAIN}\\${config.PLATFORM_NAME}-${config.EVB_INTERFACE}-${config.EVB_TYPE}.bin"               
					bat "cd projects/${projectName}/tests & pytest --rootdir=${WORKSPACE}\\projects\\${projectName}\\tests -c pytest.ini --serialnumber ${platformInfo["serialnumber"]}  --serialport ${platformInfo["serialport"]} --mountpoint ${platformInfo["mountpoint"]}"			
					archiveArtifacts allowEmptyArchive: true, artifacts: "projects/${projectName}/tests/output/*.csv"
					junit allowEmptyResults:true, testResults: "projects/${projectName}/tests/output/*.xml"
					deleteDir()
				}
			}
			catch (Exception ex) {
				echo "Failed in Test-${config.PLATFORM_NAME}-${config.EVB_INTERFACE}-${config.EVB_TYPE} stage"
				echo "Caught:${ex}"
				currentBuild.result = 'FAILURE'
				deleteDir()
			}
		}
	}
}

return this;