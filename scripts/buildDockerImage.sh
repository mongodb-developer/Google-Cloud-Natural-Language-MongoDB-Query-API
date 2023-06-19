#Custom Docker Image
docker build --platform linux/amd64 -t gcr.io/peer-poc/genai-api:v1 .
docker push gcr.io/peer-poc/genai-api:v1

#gcloud set project peer-poc
gcloud config set project peer-poc

#Set environment variables
export GENAI_PREDICT_ENDPOINT=""
export GENAI_TEXT_EMBEDDING_ENDPOINT=""
export MONGODB_URI=""
export GOOGLE_APPLICATION_CREDENTIALS=""
export GENAI_DB="genai"
export PORT=8080

#Using JIB
gcloud projects create genai-mongodb-demo --set-as-default
gcloud auth configure-docker gcr.io
./gradlew build
./gradlew publishImage

#Test the image locally
PORT=8080 && docker run \
-p ${PORT}:${PORT} \
-e PORT=${PORT} \
-e GOOGLE_APPLICATION_CREDENTIALS=/tmp/keys/gcloud_creds.json \
-e GENAI_PREDICT_ENDPOINT="$GENAI_PREDICT_ENDPOINT" \
-e GENAI_TEXT_EMBEDDING_ENDPOINT="$GENAI_TEXT_EMBEDDING_ENDPOINT" \
-e MONGODB_URI="$MONGODB_URI" \
-v $GOOGLE_APPLICATION_CREDENTIALS:/tmp/keys/gcloud_creds.json:ro \
gcr.io/peer-poc/genai-api:v1

#Deploy to Cloud Run
gcloud run deploy genai-api \
--image gcr.io/peer-poc/genai-api:v1 \
--platform managed \
--region us-central1 \
--allow-unauthenticated \
--set-env-vars=GENAI_PREDICT_ENDPOINT="$GENAI_PREDICT_ENDPOINT",GENAI_TEXT_EMBEDDING_ENDPOINT="$GENAI_TEXT_EMBEDDING_ENDPOINT",MONGODB_URI="$MONGODB_URI",GENAI_DB="$GENAI_DB"
