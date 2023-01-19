# Terraform for provisioning AWS game server infrastructure

## Executing the Terraform Code

get output tu step 1 sang step 2 :

Khac module: step 1 phai khai bao output ---> step get sang: exam: vpc_id = module.networking.vpc_id ---> get vpc_id tao ra tu module networking

Cung 1 module : call ra truc tiep : vpc_id = aws_vpc.aws-vpc.id 

co nhieu moi truong thi config file.tfvar de trong thu muc , khi run lenh apply ---> tro den file do. ko tro se lay mac dinh thu muc hien tai